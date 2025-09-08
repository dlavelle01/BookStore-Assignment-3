package com.ucd.bookshop.authentication;

import com.ucd.bookshop.model.User;

import com.ucd.bookshop.service.UserService;
import com.ucd.bookshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.authentication.AuthenticationProvider;
import com.ucd.bookshop.authentication.TwoFactorAuthRequiredException;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final UserService userService;


    public CustomAuthenticationProvider(UserRepository userRepository,
                                        UserDetailsService userDetailsService,
                                        UserService userService) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws org.springframework.security.core.AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : "";

        // 1) Fetch the user (your repo returns null if missing)
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 2) Verify password using your existing service
        try {
            if (!userService.verifyPassword(rawPassword, user.getSalt(), user.getPassword())) {
                throw new BadCredentialsException("Invalid username or password");
            }
        } catch (Exception e) {
            throw new BadCredentialsException("Authentication failed");
        }
/*
        // 3) If 2FA is enabled, at least require a code to be present
        //    (We can wire real TOTP verification later—keeping this compile-only.)
        if (Boolean.TRUE.equals(user.getIsUsing2FA())){
            String code = null;
            Object details = authentication.getDetails();
            if (details instanceof CustomWebAuthenticationDetails) {
                code = ((CustomWebAuthenticationDetails) details).getVerificationCode();
            }
            if (code == null || code.isBlank()) {
                throw new BadCredentialsException("2FA code required");
            }
            // TODO: verify TOTP code against user.getSecret() if you want full 2FA enforcement now
        }

 */
        // 3) 2FA handling — trigger two-page flow or verify inline if code present
        boolean twoFaEnabled = Boolean.TRUE.equals(user.getIsUsing2FA()); // or user.isUsing2FA()
        String code = null;
        Object details = authentication.getDetails();
        if (details instanceof CustomWebAuthenticationDetails d) {
            code = d.getVerificationCode();
        }
        if (twoFaEnabled) {
            if (code == null || code.isBlank()) {
                // CRITICAL: use custom exception so failureHandler redirects to /login2
                throw new TwoFactorAuthRequiredException("2FA code required");
            }
            if (user.getSecret() == null || user.getSecret().isBlank()) {
                throw new BadCredentialsException("2FA not set up for this account");
            }
            Totp totp = new Totp(user.getSecret());
            if (!totp.verify(code.trim())) {
                throw new BadCredentialsException("Invalid 2FA code");
            }
        }




        // 4) Load authorities via UserDetailsService you already have configured
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 5) Return successful auth token
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }



    @Override
    public boolean supports(Class<?> authentication) {
        //return authentication.equals(UsernamePasswordAuthenticationToken.class);
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}

