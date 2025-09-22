package com.memorizewords.repository;

import com.memorizewords.entity.AccountStatus;
import com.memorizewords.entity.Provider;
import com.memorizewords.entity.User;
import com.memorizewords.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository interface for user data access with authentication methods.
 *
 * Extends JpaRepository to provide CRUD operations and custom queries
 * specifically for authentication and user management operations.
 *
 * @author Memorize Words Team
 * @version 1.0.0
 * @since 2025-09-22
 */
@Repository
@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by either username or email address.
     * Useful for login operations where users can enter either.
     *
     * @param usernameOrEmail the username or email to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by password reset token.
     *
     * @param passwordResetToken the password reset token to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    /**
     * Finds a user by email verification token.
     *
     * @param emailVerificationToken the email verification token to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

    /**
     * Finds a user by refresh token.
     *
     * @param refreshToken the refresh token to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Finds a user by provider and provider ID for OAuth authentication.
     *
     * @param provider the authentication provider
     * @param providerId the provider-specific user ID
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    /**
     * Finds all users with a specific role.
     *
     * @param role the role to search for
     * @return a list of users with the specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Finds all users with a specific account status.
     *
     * @param accountStatus the account status to search for
     * @return a list of users with the specified account status
     */
    List<User> findByAccountStatus(AccountStatus accountStatus);

    /**
     * Finds users whose accounts are locked.
     *
     * @return a list of users with locked accounts
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedAccounts();

    /**
     * Finds users with unverified email addresses.
     *
     * @return a list of users with unverified emails
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Finds users who need to verify their email (token not expired).
     *
     * @return a list of users with pending email verification
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.emailVerificationExpiry > CURRENT_TIMESTAMP")
    List<User> findUsersWithPendingEmailVerification();

    /**
     * Finds users with expired email verification tokens.
     *
     * @return a list of users with expired email verification tokens
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.emailVerificationExpiry <= CURRENT_TIMESTAMP AND u.emailVerificationToken IS NOT NULL")
    List<User> findUsersWithExpiredEmailVerification();

    /**
     * Finds users with expired password reset tokens.
     *
     * @return a list of users with expired password reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetExpiry <= CURRENT_TIMESTAMP AND u.passwordResetToken IS NOT NULL")
    List<User> findUsersWithExpiredPasswordResetTokens();

    /**
     * Finds users with failed login attempts greater than a threshold.
     *
     * @param threshold the minimum number of failed attempts
     * @return a list of users with failed login attempts exceeding the threshold
     */
    List<User> findByFailedLoginAttemptsGreaterThan(Integer threshold);

    /**
     * Finds users by account status and email verification status.
     *
     * @param accountStatus the account status
     * @param emailVerified the email verification status
     * @return a list of users matching both criteria
     */
    List<User> findByAccountStatusAndEmailVerified(AccountStatus accountStatus, Boolean emailVerified);

    /**
     * Finds users who have been inactive since a specific date.
     *
     * @param lastActivityBefore the cutoff date for inactivity
     * @return a list of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :lastActivityBefore OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("lastActivityBefore") LocalDateTime lastActivityBefore);

    /**
     * Updates the password hash for a user.
     *
     * @param userId the ID of the user to update
     * @param newPasswordHash the new password hash
     * @param passwordChangedAt the timestamp when the password was changed
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :newPasswordHash, u.passwordChangedAt = :passwordChangedAt WHERE u.id = :userId")
    void updatePasswordHash(@Param("userId") Long userId, @Param("newPasswordHash") String newPasswordHash, @Param("passwordChangedAt") LocalDateTime passwordChangedAt);

    /**
     * Updates the email verification status for a user.
     *
     * @param userId the ID of the user to update
     * @param emailVerified the new email verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :emailVerified, u.emailVerificationToken = NULL, u.emailVerificationExpiry = NULL WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, @Param("emailVerified") Boolean emailVerified);

    /**
     * Updates the account status for a user.
     *
     * @param userId the ID of the user to update
     * @param accountStatus the new account status
     */
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :accountStatus WHERE u.id = :userId")
    void updateAccountStatus(@Param("userId") Long userId, @Param("accountStatus") AccountStatus accountStatus);

    /**
     * Resets failed login attempts for a user.
     *
     * @param userId the ID of the user to update
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountLockedUntil = NULL WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Increments failed login attempts for a user.
     *
     * @param userId the ID of the user to update
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Locks a user account until a specific time.
     *
     * @param userId the ID of the user to lock
     * @param lockUntil the time until which the account should be locked
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Clears the refresh token for a user.
     *
     * @param userId the ID of the user to update
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL, u.refreshTokenExpiry = NULL WHERE u.id = :userId")
    void clearRefreshToken(@Param("userId") Long userId);

    /**
     * Sets the refresh token for a user.
     *
     * @param userId the ID of the user to update
     * @param refreshToken the refresh token to set
     * @param expiry the expiry time of the refresh token
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken, u.refreshTokenExpiry = :expiry WHERE u.id = :userId")
    void setRefreshToken(@Param("userId") Long userId, @Param("refreshToken") String refreshToken, @Param("expiry") LocalDateTime expiry);

    /**
     * Records a login for a user.
     *
     * @param userId the ID of the user to update
     * @param loginAt the login timestamp
     * @param ipAddress the IP address from which the user logged in
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt, u.lastLoginIp = :ipAddress, u.failedLoginAttempts = 0, u.accountLockedUntil = NULL WHERE u.id = :userId")
    void recordLogin(@Param("userId") Long userId, @Param("loginAt") LocalDateTime loginAt, @Param("ipAddress") String ipAddress);

    /**
     * Deletes expired email verification tokens.
     *
     * @return the number of tokens deleted
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerificationToken = NULL, u.emailVerificationExpiry = NULL WHERE u.emailVerificationExpiry <= CURRENT_TIMESTAMP")
    int deleteExpiredEmailVerificationTokens();

    /**
     * Deletes expired password reset tokens.
     *
     * @return the number of tokens deleted
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetExpiry = NULL WHERE u.passwordResetExpiry <= CURRENT_TIMESTAMP")
    int deleteExpiredPasswordResetTokens();

    /**
     * Finds users who need their accounts locked due to too many failed attempts.
     *
     * @param maxAttempts the maximum allowed failed attempts
     * @return a list of users who should be locked
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.accountLockedUntil IS NULL")
    List<User> findUsersToLock(@Param("maxAttempts") Integer maxAttempts);

    /**
     * Counts users by role for statistics.
     *
     * @param role the role to count
     * @return the number of users with the specified role
     */
    long countByRole(UserRole role);

    /**
     * Counts users by account status for statistics.
     *
     * @param accountStatus the account status to count
     * @return the number of users with the specified account status
     */
    long countByAccountStatus(AccountStatus accountStatus);

    /**
     * Counts verified users.
     *
     * @return the number of verified users
     */
    long countByEmailVerifiedTrue();

    /**
     * Counts users created after a specific date.
     *
     * @param date the date to count from
     * @return the number of users created after the specified date
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Finds users who logged in within a specific date range.
     *
     * @param startDate the start of the date range
     * @param endDate the end of the date range
     * @return a list of users who logged in within the specified range
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    List<User> findUsersWhoLoggedInBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Finds users by account creation IP address for security analysis.
     *
     * @param createdIp the IP address to search for
     * @return a list of users created from the specified IP address
     */
    List<User> findByCreatedIp(String createdIp);

    /**
     * Finds users by last login IP address for security analysis.
     *
     * @param lastLoginIp the IP address to search for
     * @return a list of users who last logged in from the specified IP address
     */
    List<User> findByLastLoginIp(String lastLoginIp);
}