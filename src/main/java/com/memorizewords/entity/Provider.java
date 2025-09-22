package com.memorizewords.entity;

/**
 * Authentication provider enumeration for social login support.
 *
 * Defines the different authentication providers that users can use
 * to register and log in to the application, supporting both local
 * and OAuth-based authentication methods.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
public enum Provider {
    /**
     * Local authentication using username/email and password.
     * User registers directly with the application using email verification.
     */
    LOCAL("LOCAL"),

    /**
     * Google OAuth 2.0 authentication.
     * User authenticates using their Google account.
     */
    GOOGLE("GOOGLE"),

    /**
     * Facebook OAuth 2.0 authentication.
     * User authenticates using their Facebook account.
     */
    FACEBOOK("FACEBOOK"),

    /**
     * Apple Sign In authentication.
     * User authenticates using their Apple ID.
     */
    APPLE("APPLE");

    private final String provider;

    Provider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    /**
     * Converts a string provider name to the corresponding Provider enum.
     *
     * @param provider the string representation of the provider
     * @return the corresponding Provider enum
     * @throws IllegalArgumentException if the provider is not valid
     */
    public static Provider fromString(String provider) {
        for (Provider authProvider : Provider.values()) {
            if (authProvider.getProvider().equalsIgnoreCase(provider)) {
                return authProvider;
            }
        }
        throw new IllegalArgumentException("Invalid authentication provider: " + provider);
    }

    /**
     * Checks if this provider is a social/OAuth provider.
     *
     * @return true if the provider is a social/OAuth provider, false otherwise
     */
    public boolean isSocial() {
        return this != LOCAL;
    }

    /**
     * Checks if this provider requires email verification.
     *
     * @return true if the provider requires email verification, false otherwise
     */
    public boolean requiresEmailVerification() {
        // Social providers typically verify email during OAuth process
        return this == LOCAL;
    }

    /**
     * Checks if this provider supports password-based authentication.
     *
     * @return true if the provider supports passwords, false otherwise
     */
    public boolean supportsPassword() {
        return this == LOCAL;
    }

    /**
     * Gets the display name for the provider.
     *
     * @return the user-friendly display name
     */
    public String getDisplayName() {
        switch (this) {
            case LOCAL:
                return "Email";
            case GOOGLE:
                return "Google";
            case FACEBOOK:
                return "Facebook";
            case APPLE:
                return "Apple";
            default:
                return name();
        }
    }
}