package com.ucd.bookshop.config;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.model.User;
import com.ucd.bookshop.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by "userName" (your domain field), maps roleId -> Spring authority.
     * Returns a Spring Security UserDetails with a single ROLE_* authority.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUserName(username);
        if (u == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        // Map roleId -> "ROLE_ADMIN" / "ROLE_CUSTOMER" (fallback to "ROLE_USER" if unknown)
        String authority = toSpringRole(u.getRoleId());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUserName())
                .password(u.getPassword()) // hashed password from DB
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private String toSpringRole(Integer roleId) {
        String roleName = (roleId != null) ? Role.getNameById(roleId) : "UNKNOWN"; // "ADMIN"/"CUSTOMER"/"UNKNOWN"
        if ("UNKNOWN".equals(roleName) || roleName == null || roleName.isBlank()) {
            return "ROLE_USER";
        }
        return "ROLE_" + roleName; // e.g., ROLE_ADMIN, ROLE_CUSTOMER
    }
}
