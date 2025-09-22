package com.memorizewords.entity;

/**
 * Account status enumeration for user account lifecycle management.
 *
 * Defines the different states that a user account can be in,
 * supporting account management, security, and compliance requirements.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
public enum AccountStatus {
    /**
     * Active account with full access to all features.
     * User can log in, use the application, and access all permitted functionality.
     */
    ACTIVE("ACTIVE"),

    /**
     * Suspended account with limited access.
     * Account is temporarily disabled due to policy violations, security concerns, or administrative action.
     * User cannot log in until the suspension is lifted.
     */
    SUSPENDED("SUSPENDED"),

    /**
     * Deleted account that has been marked for deletion.
     * Account is permanently removed from active use but may be retained for compliance or backup purposes.
     * User cannot log in and data may be scheduled for permanent deletion.
     */
    DELETED("DELETED");

    private final String status;

    AccountStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Converts a string status name to the corresponding AccountStatus enum.
     *
     * @param status the string representation of the status
     * @return the corresponding AccountStatus enum
     * @throws IllegalArgumentException if the status is not valid
     */
    public static AccountStatus fromString(String status) {
        for (AccountStatus accountStatus : AccountStatus.values()) {
            if (accountStatus.getStatus().equalsIgnoreCase(status)) {
                return accountStatus;
            }
        }
        throw new IllegalArgumentException("Invalid account status: " + status);
    }

    /**
     * Checks if the account is currently active.
     *
     * @return true if the account is active, false otherwise
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Checks if the account is suspended.
     *
     * @return true if the account is suspended, false otherwise
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    /**
     * Checks if the account is deleted.
     *
     * @return true if the account is deleted, false otherwise
     */
    public boolean isDeleted() {
        return this == DELETED;
    }

    /**
     * Checks if the user can log in with this status.
     *
     * @return true if the user can log in, false otherwise
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Checks if the account is accessible for administrative purposes.
     *
     * @return true if the account can be accessed by administrators, false otherwise
     */
    public boolean isAccessible() {
        return this != DELETED;
    }
}