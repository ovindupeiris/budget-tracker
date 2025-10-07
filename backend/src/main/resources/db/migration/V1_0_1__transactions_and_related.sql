-- Budget Tracker Application - Transactions and Related Tables
-- Version: 1.0.1
-- Description: Create transactions, budgets, goals, and supporting tables

-- ============================================================================
-- TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19, 8) DEFAULT 1.00000000,
    amount_in_wallet_currency DECIMAL(19, 4),
    transaction_date DATE NOT NULL,
    description VARCHAR(500),
    notes TEXT,
    merchant_name VARCHAR(200),
    location VARCHAR(300),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',

    -- Recurring transaction link
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_template_id UUID REFERENCES recurring_transactions(id) ON DELETE SET NULL,

    -- Reconciliation
    is_reconciled BOOLEAN NOT NULL DEFAULT FALSE,
    reconciled_at TIMESTAMP,

    -- Transfer fields
    from_wallet_id UUID REFERENCES wallets(id) ON DELETE SET NULL,
    to_wallet_id UUID REFERENCES wallets(id) ON DELETE SET NULL,
    transfer_fee DECIMAL(19, 4),
    linked_transaction_id UUID,

    -- Bank sync fields
    bank_transaction_id VARCHAR(255),
    bank_imported BOOLEAN NOT NULL DEFAULT FALSE,
    bank_imported_at TIMESTAMP,
    bank_pending BOOLEAN NOT NULL DEFAULT FALSE,

    -- Attachments
    receipt_url VARCHAR(500),
    has_attachments BOOLEAN NOT NULL DEFAULT FALSE,

    -- Splits
    is_split BOOLEAN NOT NULL DEFAULT FALSE,
    parent_transaction_id UUID REFERENCES transactions(id) ON DELETE CASCADE,

    -- Subscription link
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,

    -- Investment fields
    investment_quantity DECIMAL(19, 8),
    investment_price_per_unit DECIMAL(19, 8),
    investment_symbol VARCHAR(20),

    -- Auto-categorization
    auto_categorized BOOLEAN NOT NULL DEFAULT FALSE,
    ml_confidence_score DECIMAL(5, 4),

    -- Metadata
    reference_number VARCHAR(100),
    external_id VARCHAR(255),
    metadata JSONB,

    -- Offline sync
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    device_id VARCHAR(100),
    conflict_resolved BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER', 'INVESTMENT',
                                                      'DIVIDEND', 'INTEREST', 'FEE', 'REFUND',
                                                      'ADJUSTMENT', 'LOAN_PAYMENT', 'LOAN_DISBURSEMENT')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED',
                                                         'FAILED', 'SCHEDULED', 'PROCESSING'))
);

CREATE INDEX idx_transaction_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transaction_category_id ON transactions(category_id);
CREATE INDEX idx_transaction_type ON transactions(type);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_created_at ON transactions(created_at);
CREATE INDEX idx_transaction_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transaction_bank_id ON transactions(bank_transaction_id);
CREATE INDEX idx_transaction_deleted ON transactions(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- TRANSACTION TAGS (Many-to-Many)
-- ============================================================================
CREATE TABLE transaction_tags (
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (transaction_id, tag_id)
);

CREATE INDEX idx_transaction_tags_transaction ON transaction_tags(transaction_id);
CREATE INDEX idx_transaction_tags_tag ON transaction_tags(tag_id);

-- ============================================================================
-- RECURRING TRANSACTION TAGS (Many-to-Many)
-- ============================================================================
CREATE TABLE recurring_transaction_tags (
    recurring_transaction_id UUID NOT NULL REFERENCES recurring_transactions(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (recurring_transaction_id, tag_id)
);

CREATE INDEX idx_recurring_tags_recurring ON recurring_transaction_tags(recurring_transaction_id);
CREATE INDEX idx_recurring_tags_tag ON recurring_transaction_tags(tag_id);

-- ============================================================================
-- TRANSACTION ATTACHMENTS TABLE
-- ============================================================================
CREATE TABLE transaction_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255),
    file_type VARCHAR(100),
    file_size BIGINT,
    file_url VARCHAR(500) NOT NULL,
    storage_key VARCHAR(500),
    thumbnail_url VARCHAR(500),
    is_receipt BOOLEAN NOT NULL DEFAULT FALSE,
    ocr_extracted BOOLEAN NOT NULL DEFAULT FALSE,
    ocr_text TEXT,
    ocr_data JSONB,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_attachment_transaction_id ON transaction_attachments(transaction_id);
CREATE INDEX idx_attachment_created_at ON transaction_attachments(created_at);

-- ============================================================================
-- TRANSACTION SPLITS TABLE
-- ============================================================================
CREATE TABLE transaction_splits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    amount DECIMAL(19, 4) NOT NULL,
    description VARCHAR(500),
    split_order INTEGER,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_split_transaction_id ON transaction_splits(parent_transaction_id);
CREATE INDEX idx_split_category_id ON transaction_splits(category_id);

-- ============================================================================
-- BUDGETS TABLE
-- ============================================================================
CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    wallet_id UUID REFERENCES wallets(id) ON DELETE SET NULL,
    amount DECIMAL(19, 4) NOT NULL,
    spent DECIMAL(19, 4) DEFAULT 0.0000,
    currency_code VARCHAR(3) NOT NULL,
    period VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    alert_threshold DECIMAL(5, 2) DEFAULT 80.00,
    alert_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    alert_sent BOOLEAN NOT NULL DEFAULT FALSE,
    rollover_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    rollover_amount DECIMAL(19, 4),
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    color VARCHAR(20),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_budget_period CHECK (period IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY',
                                                     'QUARTERLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT chk_budget_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'ARCHIVED'))
);

CREATE INDEX idx_budget_user_id ON budgets(user_id);
CREATE INDEX idx_budget_category_id ON budgets(category_id);
CREATE INDEX idx_budget_wallet_id ON budgets(wallet_id);
CREATE INDEX idx_budget_period ON budgets(period);
CREATE INDEX idx_budget_dates ON budgets(start_date, end_date);
CREATE INDEX idx_budget_status ON budgets(status);
CREATE INDEX idx_budget_deleted ON budgets(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- SAVINGS GOALS TABLE
-- ============================================================================
CREATE TABLE savings_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_id UUID REFERENCES wallets(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0.0000,
    currency_code VARCHAR(3) NOT NULL,
    target_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    icon VARCHAR(50),
    color VARCHAR(20),
    priority INTEGER,
    auto_save_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_save_amount DECIMAL(19, 4),
    auto_save_frequency VARCHAR(20),
    completed_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_goal_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED', 'ARCHIVED'))
);

CREATE INDEX idx_goal_user_id ON savings_goals(user_id);
CREATE INDEX idx_goal_wallet_id ON savings_goals(wallet_id);
CREATE INDEX idx_goal_status ON savings_goals(status);
CREATE INDEX idx_goal_target_date ON savings_goals(target_date);
CREATE INDEX idx_goal_deleted ON savings_goals(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- WALLET SHARES TABLE
-- ============================================================================
CREATE TABLE wallet_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    shared_with_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shared_by_email VARCHAR(255),
    permission VARCHAR(20) NOT NULL DEFAULT 'VIEW',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invitation_token VARCHAR(255),
    invitation_expires_at TIMESTAMP,
    accepted_at TIMESTAMP,
    can_add_transactions BOOLEAN NOT NULL DEFAULT FALSE,
    can_edit_transactions BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete_transactions BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_budget BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT uk_wallet_user_share UNIQUE (wallet_id, shared_with_user_id),
    CONSTRAINT chk_share_permission CHECK (permission IN ('VIEW', 'EDIT', 'ADMIN')),
    CONSTRAINT chk_share_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_wallet_share_wallet_id ON wallet_shares(wallet_id);
CREATE INDEX idx_wallet_share_shared_with_user_id ON wallet_shares(shared_with_user_id);
CREATE INDEX idx_wallet_share_status ON wallet_shares(status);

-- ============================================================================
-- CATEGORY RULES TABLE
-- ============================================================================
CREATE TABLE category_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    condition VARCHAR(30) NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    field_value VARCHAR(500) NOT NULL,
    priority INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    apply_count INTEGER DEFAULT 0,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_rule_condition CHECK (condition IN ('CONTAINS', 'EQUALS', 'STARTS_WITH',
                                                        'ENDS_WITH', 'GREATER_THAN', 'LESS_THAN', 'REGEX'))
);

CREATE INDEX idx_rule_user_id ON category_rules(user_id);
CREATE INDEX idx_rule_category_id ON category_rules(category_id);
CREATE INDEX idx_rule_priority ON category_rules(priority);
CREATE INDEX idx_rule_active ON category_rules(is_active);
CREATE INDEX idx_rule_deleted ON category_rules(deleted) WHERE deleted = FALSE;

-- ============================================================================
-- NOTIFICATIONS TABLE
-- ============================================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP,
    scheduled_for TIMESTAMP,
    priority INTEGER DEFAULT 0,
    related_entity_type VARCHAR(100),
    related_entity_id UUID,
    action_url VARCHAR(500),
    metadata JSONB,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    error_message VARCHAR(1000),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'PUSH', 'SMS', 'IN_APP')),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_read ON notifications(is_read);

-- ============================================================================
-- AUDIT LOGS TABLE
-- ============================================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID,
    username VARCHAR(100),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(1000),
    metadata JSONB,
    request_id VARCHAR(100)
);

CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_ip_address ON audit_logs(ip_address);

-- ============================================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================================

-- View for user wallet summary
CREATE VIEW vw_user_wallet_summary AS
SELECT
    u.id AS user_id,
    COUNT(w.id) AS total_wallets,
    SUM(CASE WHEN w.deleted = FALSE AND w.is_archived = FALSE THEN 1 ELSE 0 END) AS active_wallets,
    SUM(CASE WHEN w.is_archived = TRUE THEN 1 ELSE 0 END) AS archived_wallets,
    SUM(w.balance) AS total_balance
FROM users u
LEFT JOIN wallets w ON u.id = w.user_id
GROUP BY u.id;

-- View for monthly transaction summary
CREATE VIEW vw_monthly_transaction_summary AS
SELECT
    user_id,
    DATE_TRUNC('month', transaction_date) AS month,
    currency_code,
    SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS total_income,
    SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS total_expenses,
    COUNT(*) AS transaction_count
FROM transactions
WHERE deleted = FALSE
GROUP BY user_id, DATE_TRUNC('month', transaction_date), currency_code;
