package com.memorizewords.entity;

/**
 * User role enumeration for authorization and access control.
 *
 * Defines the different roles that users can have in the application,
 * supporting role-based access control (RBAC) for authentication and authorization.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
public enum UserRole {
    /**
     * Regular user with basic application access.
     * Can create word collections, study words, and manage their own learning progress.
     */
    USER("USER"),

    /**
     * Moderator role with additional permissions for content management.
     * Can manage word collections, moderate user-generated content, and help with community management.
     */
    MODERATOR("MODERATOR"),

    /**
     * Administrator with full system access.
     * Can manage users, system configuration, and have access to all administrative features.
     */
    ADMIN("ADMIN");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    /**
     * Converts a string role name to the corresponding UserRole enum.
     *
     * @param role the string representation of the role
     * @return the corresponding UserRole enum
     * @throws IllegalArgumentException if the role is not valid
     */
    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.getRole().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid user role: " + role);
    }

    /**
     * Checks if this role has administrative privileges.
     *
     * @return true if the role has admin privileges, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if this role has moderator privileges.
     *
     * @return true if the role has moderator privileges, false otherwise
     */
    public boolean isModerator() {
        return this == MODERATOR || this == ADMIN;
    }

    /**
     * Checks if this role can manage other users.
     *
     * @return true if the role can manage users, false otherwise
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }

    /**
     * Checks if this role can manage content.
     *
     * @return true if the role can manage content, false otherwise
     */
    public boolean canManageContent() {
        return this == MODERATOR || this == ADMIN;
    }
}