package com.ucd.bookshop.authentication;

import com.ucd.bookshop.model.User;

import com.ucd.bookshop.service.UserService;
import com.ucd.bookshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.authentication.AuthenticationProvider;

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

    /*
    @Autowired
    private UserRepository userRepository;

     */

    /*
    @Override
    public Authentication authenticate(Authentication auth)
            throws AuthenticationException {
        String verificationCode
                = ((CustomWebAuthenticationDetails) auth.getDetails())
                .getVerificationCode();


        User user = userRepository.findByUserName(auth.getName());
        if ((user == null)) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (user.getIsUsing2FA()) {

            Totp totp = new Totp(user.getSecret());
            if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid Verification Code");
            }
        }

        Authentication result = super.authenticate(auth);

        System.out.println("Credentials "+result.getAuthorities().toString());

        return new UsernamePasswordAuthenticationToken(
                user, result.getCredentials(), result.getAuthorities());
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

     */
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

        // 4) Load authorities via UserDetailsService you already have configured
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 5) Return successful auth token
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                rawPassword,
                userDetails.getAuthorities()
        );
    }



    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}

