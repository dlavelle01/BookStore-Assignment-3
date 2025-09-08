package com.ucd.bookshop.security;

import com.ucd.bookshop.authentication.TwoFactorAuthRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class TwoFactorFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws java.io.IOException {
        if (exception instanceof TwoFactorAuthRequiredException) {
            String username = request.getParameter("username");
            request.getSession(true).setAttribute("MFA_USERNAME", username);
            // Optional: short-lived timestamp for extra safety
            request.getSession().setAttribute("MFA_TS", System.currentTimeMillis());
            response.sendRedirect(request.getContextPath() + "/v1/web/users/login2");
        } else {
            response.sendRedirect(request.getContextPath() + "/v1/web/users/login?error=true");
        }
    }
}
