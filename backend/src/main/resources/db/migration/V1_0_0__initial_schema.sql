-- Budget Tracker Application - Initial Schema Migration
-- Version: 1.0.0
-- Description: Create all core tables for the application

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    profile_picture_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_token_expires_at TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expires_at TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(255),

    -- User Preferences (embedded)
    pref_date_format VARCHAR(20) DEFAULT 'YYYY-MM-DD',
    pref_time_format VARCHAR(20) DEFAULT '24H',
    pref_first_day_of_week INTEGER DEFAULT 1,
    pref_notifications_enabled BOOLEAN DEFAULT TRUE,
    pref_email_notifications BOOLEAN DEFAULT TRUE,
    pref_push_notifications BOOLEAN DEFAULT FALSE,
    pref_budget_alerts BOOLEAN DEFAULT TRUE,
    pref_transaction_alerts BOOLEAN DEFAULT TRUE,
    pref_bill_reminders BOOLEAN DEFAULT TRUE,
    pref_theme VARCHAR(20) DEFAULT 'LIGHT',
    pref_language VARCHAR(10) DEFAULT 'en',
    pref_show_balance_on_login BOOLEAN DEFAULT TRUE,
    pref_biometric_auth_enabled BOOLEAN DEFAULT FALSE,

    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en',
    currency_code VARCHAR(3) DEFAULT 'USD',
    stripe_customer_id VARCHAR(255),
    subscription_tier VARCHAR(20) DEFAULT 'FREE',
    subscription_expires_at TIMESTAMP,
    terms_accepted_at TIMESTAMP,
    privacy_accepted_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'CLOSED'))
);

-- User Roles Table (ElementCollection mapping)
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'PREMIUM', 'ADMIN', 'SUPER_ADMIN', 'SUPPORT'))
);

-- Indexes for users table
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_subscription_tier ON users(subscription_tier);
CREATE INDEX idx_user_deleted ON users(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- WALLETS TABLE
-- ============================================================================
CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(30) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    initial_balance DECIMAL(19, 4) DEFAULT 0.0000,
    icon VARCHAR(50),
    color VARCHAR(20),
    display_order INTEGER DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    exclude_from_totals BOOLEAN NOT NULL DEFAULT FALSE,

    -- Bank integration fields
    bank_name VARCHAR(100),
    account_number_last_four VARCHAR(4),
    bank_connection_id VARCHAR(255),
    bank_account_id VARCHAR(255),
    sync_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_synced_at TIMESTAMP,

    -- Credit card specific fields
    credit_limit DECIMAL(19, 4),
    available_credit DECIMAL(19, 4),
    billing_cycle_day INTEGER,
    payment_due_day INTEGER,

    -- Additional fields
    account_holder_name VARCHAR(200),
    institution_name VARCHAR(200),
    account_type VARCHAR(50),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_wallet_type CHECK (type IN ('CASH', 'CHECKING', 'SAVINGS', 'CREDIT_CARD', 'DEBIT_CARD',
                                                'INVESTMENT', 'LOAN', 'MORTGAGE', 'CRYPTO', 'E_WALLET',
                                                'PREPAID', 'OTHER'))
);

CREATE INDEX idx_wallet_user_id ON wallets(user_id);
CREATE INDEX idx_wallet_type ON wallets(type);
CREATE INDEX idx_wallet_currency ON wallets(currency_code);
CREATE INDEX idx_wallet_created_at ON wallets(created_at);
CREATE INDEX idx_wallet_is_default ON wallets(is_default);
CREATE INDEX idx_wallet_deleted ON wallets(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    display_order INTEGER DEFAULT 0,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_category_id UUID REFERENCES categories(id) ON DELETE SET NULL,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE', 'BOTH'))
);

CREATE INDEX idx_category_user_id ON categories(user_id);
CREATE INDEX idx_category_type ON categories(type);
CREATE INDEX idx_category_parent_id ON categories(parent_category_id);
CREATE INDEX idx_category_system ON categories(is_system);
CREATE INDEX idx_category_deleted ON categories(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- TAGS TABLE
-- ============================================================================
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    description VARCHAR(500),
    usage_count INTEGER DEFAULT 0,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT uk_tag_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_tag_user_id ON tags(user_id);
CREATE INDEX idx_tag_name ON tags(name);
CREATE INDEX idx_tag_deleted ON tags(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- RECURRING TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    interval_count INTEGER DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence_date DATE,
    last_occurrence_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    auto_create BOOLEAN NOT NULL DEFAULT TRUE,
    notification_days_before INTEGER DEFAULT 1,
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    occurrence_count INTEGER DEFAULT 0,
    max_occurrences INTEGER,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_recurring_type CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER', 'INVESTMENT',
                                                    'DIVIDEND', 'INTEREST', 'FEE', 'REFUND',
                                                    'ADJUSTMENT', 'LOAN_PAYMENT', 'LOAN_DISBURSEMENT')),
    CONSTRAINT chk_recurring_frequency CHECK (frequency IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY',
                                                              'QUARTERLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT chk_recurring_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_recurring_user_id ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_wallet_id ON recurring_transactions(wallet_id);
CREATE INDEX idx_recurring_status ON recurring_transactions(status);
CREATE INDEX idx_recurring_next_date ON recurring_transactions(next_occurrence_date);
CREATE INDEX idx_recurring_deleted ON recurring_transactions(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- SUBSCRIPTIONS TABLE
-- ============================================================================
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    provider_name VARCHAR(100),
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    billing_frequency VARCHAR(20) NOT NULL,
    billing_day INTEGER,
    start_date DATE NOT NULL,
    next_billing_date DATE,
    last_billing_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    icon VARCHAR(50),
    color VARCHAR(20),
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    reminder_days_before INTEGER DEFAULT 3,
    reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    auto_create_transaction BOOLEAN NOT NULL DEFAULT TRUE,
    free_trial BOOLEAN NOT NULL DEFAULT FALSE,
    free_trial_end_date DATE,
    cancellation_notice_period_days INTEGER,
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_subscription_frequency CHECK (billing_frequency IN ('DAILY', 'WEEKLY', 'BIWEEKLY',
                                                                         'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT chk_subscription_status CHECK (status IN ('ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED', 'TRIAL'))
);

CREATE INDEX idx_subscription_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscription_wallet_id ON subscriptions(wallet_id);
CREATE INDEX idx_subscription_category_id ON subscriptions(category_id);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_subscription_next_billing ON subscriptions(next_billing_date);
CREATE INDEX idx_subscription_deleted ON subscriptions(deleted) WHERE deleted = FALSE;

-- This is part 1 of the migration. Continuing in next section...
