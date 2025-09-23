package com.memorizewords.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for security-related test operations.
 * Provides helper methods for security testing.
 */
public class SecurityTestUtil {

    /**
     * Clears the security context.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Gets the current authentication from security context.
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if a user is authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    /**
     * Checks if the current user has a specific role.
     */
    public static boolean hasRole(String role) {
        if (!isAuthenticated()) {
            return false;
        }
        return getCurrentAuthentication().getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(authority -> authority.equals("ROLE_" + role));
    }

    /**
     * Gets the current username.
     */
    public static String getCurrentUsername() {
        if (!isAuthenticated()) {
            return null;
        }
        return getCurrentAuthentication().getName();
    }
}