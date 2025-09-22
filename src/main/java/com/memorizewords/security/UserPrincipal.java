package com.memorizewords.security;

import com.memorizewords.entity.User;
import com.memorizewords.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * UserPrincipal class for Spring Security integration.
 *
 * This class implements the UserDetails interface and serves as a bridge
 * between the application's User entity and Spring Security's authentication system.
 * It provides user information for authentication and authorization purposes.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountVerified;
    private final boolean accountNonLocked;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    /**
     * Constructs a UserPrincipal from a User entity.
     *
     * @param user the User entity to convert to UserPrincipal
     */
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().getRole())
        );
        this.accountVerified = user.isEmailVerified();
        this.accountNonLocked = !user.isAccountLocked();
        this.accountNonExpired = user.getAccountStatus().isAccessible();
        this.credentialsNonExpired = true; // Password expiration can be implemented if needed
        this.enabled = user.isActive() && user.getAccountStatus().canLogin();
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the user's email address.
     *
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's role.
     *
     * @return the user's role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Checks if the user's email is verified.
     *
     * @return true if the email is verified, false otherwise
     */
    public boolean isAccountVerified() {
        return accountVerified;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", accountVerified=" + accountVerified +
                ", accountNonLocked=" + accountNonLocked +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * Creates a UserPrincipal from a User entity.
     * This is a convenience factory method.
     *
     * @param user the User entity to convert
     * @return the created UserPrincipal
     * @throws IllegalArgumentException if the user is null
     */
    public static UserPrincipal create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new UserPrincipal(user);
    }

    /**
     * Creates a UserPrincipal from a User entity with additional validation.
     * This method checks if the user is eligible for authentication.
     *
     * @param user the User entity to convert
     * @return the created UserPrincipal
     * @throws IllegalArgumentException if the user is null or not eligible for authentication
     */
    public static UserPrincipal createForAuthentication(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Validate user eligibility for authentication
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is not active");
        }

        if (!user.getAccountStatus().canLogin()) {
            throw new IllegalArgumentException("User account cannot login in current status: " + user.getAccountStatus());
        }

        if (user.isAccountLocked()) {
            throw new IllegalArgumentException("User account is locked until: " + user.getAccountLockedUntil());
        }

        return new UserPrincipal(user);
    }

    /**
     * Checks if the user has administrative privileges.
     *
     * @return true if the user is an administrator, false otherwise
     */
    public boolean isAdmin() {
        return role.isAdmin();
    }

    /**
     * Checks if the user has moderator privileges.
     *
     * @return true if the user is a moderator or administrator, false otherwise
     */
    public boolean isModerator() {
        return role.isModerator();
    }

    /**
     * Checks if the user can manage other users.
     *
     * @return true if the user can manage users, false otherwise
     */
    public boolean canManageUsers() {
        return role.canManageUsers();
    }

    /**
     * Checks if the user can manage content.
     *
     * @return true if the user can manage content, false otherwise
     */
    public boolean canManageContent() {
        return role.canManageContent();
    }

    /**
     * Gets a user-friendly display name for the user.
     *
     * @return the display name (username or email if username is not available)
     */
    public String getDisplayName() {
        return (username != null && !username.trim().isEmpty()) ? username : email;
    }

    /**
     * Checks if the user's account is fully verified and ready for all features.
     *
     * @return true if the account is verified and ready, false otherwise
     */
    public boolean isFullyVerified() {
        return accountVerified && enabled && accountNonLocked && accountNonExpired;
    }

    /**
     * Gets the authentication status as a user-friendly message.
     *
     * @return a string describing the authentication status
     */
    public String getAuthenticationStatus() {
        if (!enabled) {
            return "Account disabled";
        }
        if (!accountNonExpired) {
            return "Account expired";
        }
        if (!accountNonLocked) {
            return "Account locked";
        }
        if (!credentialsNonExpired) {
            return "Credentials expired";
        }
        if (!accountVerified) {
            return "Email not verified";
        }
        return "Account active and verified";
    }
}