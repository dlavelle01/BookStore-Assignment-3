package com.ucd.bookshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import org.springframework.stereotype.Component;

import com.ucd.bookshop.model.User;
import com.ucd.bookshop.repository.UserRepository;
import com.ucd.bookshop.service.UserService;
import com.ucd.bookshop.security.TwoFactorFailureHandler;
import com.ucd.bookshop.authentication.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomAuthenticationSuccessHandler customSuccessHandler;
    private final UserRepository userRepository;
    private final UserService userService;
    //private final UserDetailsService userDetailsService;
    //private final CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    public SecurityConfig(CustomAuthenticationSuccessHandler customSuccessHandler,
                          UserRepository userRepository,
                          UserService userService/*,
                          UserDetailsService userDetailsService,
                          CustomAuthenticationProvider customAuthenticationProvider*/) {
        //this.customAuthenticationProvider = customAuthenticationProvider;
        this.customSuccessHandler = customSuccessHandler;
        this.userRepository = userRepository;
        this.userService = userService;
        //this.userDetailsService = userDetailsService;
    }

    /*
   // Bean that captures extra login fields (e.g., verificationCode) on the first page
    @Bean
    public org.springframework.security.authentication.AuthenticationDetailsSource<jakarta.servlet.http.HttpServletRequest, org.springframework.security.web.authentication.WebAuthenticationDetails> authenticationDetailsSource() {
    return new CustomWebAuthenticationDetailsSource();
    }

     */

    /*
    @Bean
    AuthenticationDetailsSource<HttpServletRequest, CustomWebAuthenticationDetails>
    authenticationDetailsSource() {
        return CustomWebAuthenticationDetails::new;
    }

     */

    @Bean
    TwoFactorFailureHandler twoFactorFailureHandler() {
        return new TwoFactorFailureHandler();
    }


    @Bean
    //public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            com.ucd.bookshop.authentication.CustomAuthenticationProvider customAuthenticationProvider,
            org.springframework.security.authentication.AuthenticationDetailsSource<
                    jakarta.servlet.http.HttpServletRequest,
                    org.springframework.security.web.authentication.WebAuthenticationDetails> detailsSource,
                    TwoFactorFailureHandler twoFactorFailureHandler
    ) throws Exception {
                http
                        // - Added for CSRF
                        .csrf(csrf -> csrf
                                .ignoringRequestMatchers("/v1/api/**", "/v1/web/payments/webhook")
                                .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                        )
                        // 🔐 Force HTTPS for every request (HTTP → 302 to HTTPS)
                        .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                        // 🛡️ HSTS: tell browsers to stick to HTTPS for your domain
                        .headers(h -> h.httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .preload(false)                // set true only if you intend to preload your domain
                        .maxAgeInSeconds(31536000)))   // 1 year
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v1/web/customers/order", "/v1/web/customers/order/success", "/v1/web/customers/order/cancel").hasRole("CUSTOMER")
                        .requestMatchers(getOpenedResources()).permitAll()
                        .requestMatchers("/v1/web/users/login", "/v1/web/users/register", "/v1/web/users/login2","/v1/web/home").permitAll()
                        .requestMatchers("/v1/web/access-denied").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/v1/api/**").hasRole("ADMIN") // API endpoints require ADMIN role
                        .requestMatchers("/admin/**", "/v1/web/books/**").hasRole("ADMIN")
                        .requestMatchers("/customer/**", "/v1/web/customers/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                        .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/v1/web/access-denied")
                        .defaultAuthenticationEntryPointFor(
                            apiAuthenticationEntryPoint(), 
                            request -> request.getRequestURI().startsWith("/v1/api/")
                        ))
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(apiAuthenticationEntryPoint()))
                .authenticationProvider(customAuthenticationProvider)
                .formLogin(form -> form
                        .loginPage("/v1/web/users/login")
                        .loginProcessingUrl("/v1/web/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/v1/web/users/login?error=true")
                        .failureHandler(twoFactorFailureHandler)
                        .authenticationDetailsSource(detailsSource)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/v1/web/users/logout")
                        .logoutSuccessUrl("/v1/web/users/login?logout=true")
                        .permitAll());
                        
        return http.build();
    }

    /*
    @Bean
    //public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            com.ucd.bookshop.authentication.CustomAuthenticationProvider customAuthenticationProvider,
            org.springframework.security.authentication.AuthenticationDetailsSource<
                    jakarta.servlet.http.HttpServletRequest,
                    org.springframework.security.web.authentication.WebAuthenticationDetails> detailsSource
    ) throws Exception {
        http
                // - Added for CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/v1/api/**", "/v1/web/payments/webhook")
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                )
                // 🔐 Force HTTPS for every request (HTTP → 302 to HTTPS)
                .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                // 🛡️ HSTS: tell browsers to stick to HTTPS for your domain
                .headers(h -> h.httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .preload(false)                // set true only if you intend to preload your domain
                        .maxAgeInSeconds(31536000)))   // 1 year
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v1/web/customers/order", "/v1/web/customers/order/success", "/v1/web/customers/order/cancel").hasRole("CUSTOMER")
                        .requestMatchers(getOpenedResources()).permitAll()
                        .requestMatchers("/v1/web/users/login", "/v1/web/users/register", "/v1/web/home").permitAll()
                        .requestMatchers("/v1/web/access-denied").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/v1/api/**").hasRole("ADMIN") // API endpoints require ADMIN role
                        .requestMatchers("/admin/**", "/v1/web/books/**").hasRole("ADMIN")
                        .requestMatchers("/customer/**", "/v1/web/customers/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/v1/web/access-denied")
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint(),
                                request -> request.getRequestURI().startsWith("/v1/api/")
                        ))
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(apiAuthenticationEntryPoint()))
                .authenticationProvider(customAuthenticationProvider)
                .formLogin(form -> form
                        .loginPage("/v1/web/users/login")
                        .loginProcessingUrl("/v1/web/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/v1/web/users/login?error=true")
                        .authenticationDetailsSource(detailsSource)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/v1/web/users/logout")
                        .logoutSuccessUrl("/v1/web/users/login?logout=true")
                        .permitAll());

        return http.build();
    }

     */



    @Bean
    public PasswordEncoder passwordEncoder() {
        // We don't actually use this since we handle authentication in
        // CustomAuthenticationProvider
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                throw new UnsupportedOperationException("Use UserService.createUser() for password encoding");
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // This won't be called since we use CustomAuthenticationProvider
                return false;
            }
        };
    }


    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("BookShop API");
        return entryPoint;
    }

    // some opened endpoints without authentication
    private String[] getOpenedResources() {
        return new String[] {
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources",
                "/swagger-resources/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/webjars/**"
        };
    }

    public static class CustomUserDetails implements UserDetails {
        private final User user;
        private final Collection<? extends GrantedAuthority> authorities;
        private final Integer customerId;

        public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities, Integer customerId) {
            this.user = user;
            this.authorities = authorities;
            this.customerId = customerId;
        }

        public User getUser() {
            return user;
        }

        public Integer getCustomerId() {
            return customerId;
        }

        public String getSalt() {
            return user.getSalt();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUserName();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    @Component
    public static class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException {
            // Get user roles
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String redirectUrl = "/v1/web/home"; // default fallback

            for (GrantedAuthority authority : authorities) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    redirectUrl = "/v1/web/books/"; // Admin goes to book management
                    break;
                } else if (role.equals("ROLE_CUSTOMER")) {
                    redirectUrl = "/v1/web/customers/checkout"; // Customer goes to cart/customer pages
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        }
    }
}
