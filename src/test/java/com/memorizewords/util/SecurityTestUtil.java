package com.memorizewords.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Utility class for security-related test operations.
 * Provides helper methods and annotations for security testing.
 */

/**
 * Custom annotation for testing with a mock user.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUser.Factory.class)
public @interface WithMockCustomUser {

    String username() default "testuser";

    String[] roles() default {"USER"};

    String password() default "password";

    class Factory implements WithSecurityContextFactory<WithMockCustomUser> {

        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            // Create a mock authentication
            Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                customUser.username(),
                customUser.password(),
                java.util.Arrays.stream(customUser.roles())
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList())
            );

            context.setAuthentication(authentication);
            return context;
        }
    }
}

/**
 * Security utility methods for testing.
 */
class SecurityTestUtil {

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