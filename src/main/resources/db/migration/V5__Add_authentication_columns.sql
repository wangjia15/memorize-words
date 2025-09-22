-- Add authentication-related columns to users table for security features
-- This migration enhances the user table with fields needed for authentication,
-- password reset, email verification, and account security

-- Add failed login attempts tracking for account lockout
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0;

-- Add account lockout timestamp for security
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP NULL;

-- Add password reset token and expiry for password recovery
ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(255) NULL;

-- Add password reset token expiration
ALTER TABLE users ADD COLUMN password_reset_expiry TIMESTAMP NULL;

-- Add email verification status
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;

-- Add email verification token
ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(255) NULL;

-- Add email verification token expiration (24 hours from generation)
ALTER TABLE users ADD COLUMN email_verification_expiry TIMESTAMP NULL;

-- Add account status (active, suspended, deleted)
ALTER TABLE users ADD COLUMN account_status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE';

-- Add last login timestamp for security monitoring
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL;

-- Add last login IP address for security tracking
ALTER TABLE users ADD COLUMN last_login_ip VARCHAR(45) NULL;

-- Add password change timestamp for security compliance
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP NULL;

-- Add refresh token for JWT refresh mechanism
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(255) NULL;

-- Add refresh token expiration
ALTER TABLE users ADD COLUMN refresh_token_expiry TIMESTAMP NULL;

-- Add provider for OAuth/social login (local, google, facebook, etc.)
ALTER TABLE users ADD COLUMN provider ENUM('LOCAL', 'GOOGLE', 'FACEBOOK', 'APPLE') DEFAULT 'LOCAL';

-- Add provider ID for social login users
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255) NULL;

-- Add profile image URL
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500) NULL;

-- Add timezone for user preferences
ALTER TABLE users ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC';

-- Add email notification preferences
ALTER TABLE users ADD COLUMN email_notifications_enabled BOOLEAN DEFAULT TRUE;

-- Add account creation IP address
ALTER TABLE users ADD COLUMN created_ip VARCHAR(45) NULL;

-- Add terms accepted flag and timestamp
ALTER TABLE users ADD COLUMN terms_accepted BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN terms_accepted_at TIMESTAMP NULL;

-- Create indexes for authentication queries
CREATE INDEX idx_users_failed_login_attempts ON users(failed_login_attempts);
CREATE INDEX idx_users_account_locked_until ON users(account_locked_until);
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);
CREATE INDEX idx_users_email_verification_token ON users(email_verification_token);
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_users_provider ON users(provider);
CREATE INDEX idx_users_provider_id ON users(provider_id);
CREATE INDEX idx_users_refresh_token ON users(refresh_token);
CREATE INDEX idx_users_created_at_account_status ON users(created_at, account_status);

-- Add constraints for data integrity
ALTER TABLE users ADD CONSTRAINT chk_users_failed_login_attempts
CHECK (failed_login_attempts >= 0);

-- Add default constraint for failed login attempts
ALTER TABLE users ALTER COLUMN failed_login_attempts SET DEFAULT 0;

-- Add default constraint for email_verified
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT FALSE;

-- Add default constraint for account_status
ALTER TABLE users ALTER COLUMN account_status SET DEFAULT 'ACTIVE';

-- Add default constraint for provider
ALTER TABLE users ALTER COLUMN provider SET DEFAULT 'LOCAL';

-- Add default constraint for email_notifications_enabled
ALTER TABLE users ALTER COLUMN email_notifications_enabled SET DEFAULT TRUE;

-- Add default constraint for timezone
ALTER TABLE users ALTER COLUMN timezone SET DEFAULT 'UTC';

-- Add default constraint for terms_accepted
ALTER TABLE users ALTER COLUMN terms_accepted SET DEFAULT FALSE;

-- Update existing users to have email_verified = false if they were created before this migration
-- This ensures existing users need to verify their email addresses
UPDATE users SET email_verified = FALSE WHERE email_verified IS NULL;

-- Update existing users to have account_status = 'active' if they were created before this migration
UPDATE users SET account_status = 'ACTIVE' WHERE account_status IS NULL;

-- Update existing users to have provider = 'local' if they were created before this migration
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL;

-- Set created_at for users who don't have it (should be rare due to DEFAULT constraint)
UPDATE users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;