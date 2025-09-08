package com.ucd.bookshop.controllers.web;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.controllers.dto.LoginDto;
import com.ucd.bookshop.controllers.dto.UserDto;
import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.exception.UserRegistrationException;
import com.ucd.bookshop.service.UserRegistrationService;
import com.ucd.bookshop.service.UserService;

import jakarta.validation.Valid;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.ucd.bookshop.repository.UserRepository;
import com.ucd.bookshop.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/v1/web/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String LOGIN_REDIRECT = "users/login";
    private final UserService userService;
    private final UserRegistrationService userRegistrationService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Autowired
    public UserController(UserService userService,
                          UserRegistrationService userRegistrationService,
                          UserRepository userRepository,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userRegistrationService = userRegistrationService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationRequest", new UserRegistrationRequestDto());
        return "users/registeration";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("userRegistrationRequest") UserRegistrationRequestDto userRegistrationRequest,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            // Return to the same form with errors
            return "users/registeration";
        }

        try {
            userRegistrationService.registerUserWithCustomer(userRegistrationRequest);
            
            logger.info("User {} registered successfully", userRegistrationRequest.getUserName());
            
            // Redirect to login with username pre-filled and success message
            return "redirect:/v1/web/users/login?registered=true&username=" + userRegistrationRequest.getUserName();
            
        } catch (UserRegistrationException e) {
            logger.error("Failed to register user: {}", e.getMessage());
            model.addAttribute("registrationError", "Failed to register user. Please try again.");
            return "users/registeration";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, 
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "username", required = false) String username) {
        
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        
        if (registered != null && username != null) {
            model.addAttribute("successMessage", "Registration successful! Welcome " + username + "! Please login with your credentials.");
            model.addAttribute("prefilledUsername", username);
        }

        return LOGIN_REDIRECT;
    }

    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute("userLoginRequest") LoginDto loginRequest,
            BindingResult bindingResult,
            Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return LOGIN_REDIRECT;
        }

        UserDto userDto = null;
        try {
            userDto = userService.authenticateUser(loginRequest.getUserName(), loginRequest.getPassword());
        } catch (NoSuchAlgorithmException e) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return LOGIN_REDIRECT;
        }

        if (userDto == null) {
            // Add error message to model
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return LOGIN_REDIRECT;
        }

        if (userDto.getRole() == Role.ADMIN) {
            return "redirect:/v1/web/home";
        } else if (userDto.getRole() == Role.CUSTOMER) {
            return "redirect:/v1/web/cart";
        }

        // If authentication successful, redirect to dashboard or home
        return "redirect:/v1/web/customers/cart";
    }

    @PostMapping("/user/update/2fa")
    @ResponseBody
    public Map<String, String> modifyUser2FA(@RequestParam("use2FA") boolean use2FA)
            throws UnsupportedEncodingException {
        UserDto user = userService.updateUser2FA(use2FA);
        if (use2FA) {
            return Map.of("message", userService.generateQRUrl(user));
        }
        return Map.of("message", "2FA disabled");
    }

    @GetMapping("/login2")
    public String showLogin2(Model model,
                             HttpSession session,
                             @RequestParam(value = "error", required = false) String error) {
        String username = (String) session.getAttribute("MFA_USERNAME");
        if (username == null || username.isBlank()) {
            // No staged username → send them back to normal login
            return "redirect:/v1/web/users/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("error", error != null);
        return "users/login2"; // resolves templates/users/login2.html
    }

    @PostMapping("/login2")
    public String processLogin2(@RequestParam("code") String code,
                                HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/v1/web/users/login";

        String username = (String) session.getAttribute("MFA_USERNAME");
        if (username == null || username.isBlank()) return "redirect:/v1/web/users/login";

        // Load user + verify TOTP
        User user = userRepository.findByUserName(username);
        if (user == null || user.getSecret() == null || user.getSecret().isBlank()) {
            return "redirect:/v1/web/users/login";
        }

        Totp totp = new Totp(user.getSecret());
        if (!totp.verify(code == null ? "" : code.trim())) {
            return "redirect:/v1/web/users/login2?error=true";
        }

        // Build authenticated session
        var userDetails = userDetailsService.loadUserByUsername(username);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Cleanup MFA session flags
        session.removeAttribute("MFA_USERNAME");
        session.removeAttribute("MFA_TS");

        // Redirect similar to your CustomAuthenticationSuccessHandler
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCustomer = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (isAdmin)    return "redirect:/v1/web/books/";
        if (isCustomer) return "redirect:/v1/web/customers/checkout";
        return "redirect:/v1/web/home";
    }

    
   
}
