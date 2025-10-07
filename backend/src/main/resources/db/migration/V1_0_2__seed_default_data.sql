-- Budget Tracker Application - Seed Default Data
-- Version: 1.0.2
-- Description: Insert default system categories and configuration data

-- ============================================================================
-- DEFAULT EXPENSE CATEGORIES
-- ============================================================================

-- Food & Dining
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Food & Dining', 'Food and dining expenses', 'EXPENSE', '🍽️', '#FF6B6B', 1, TRUE, TRUE, NULL);

WITH food_cat AS (SELECT id FROM categories WHERE name = 'Food & Dining' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM food_cat)
FROM (VALUES
    ('Groceries', '🛒', '#FF6B6B', 1),
    ('Restaurants', '🍴', '#FF8787', 2),
    ('Fast Food', '🍔', '#FFA3A3', 3),
    ('Coffee & Tea', '☕', '#FFBFBF', 4),
    ('Bars & Alcohol', '🍺', '#FFDBDB', 5)
) AS t(name, icon, color, row_number);

-- Transportation
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Transportation', 'Transportation expenses', 'EXPENSE', '🚗', '#4ECDC4', 2, TRUE, TRUE, NULL);

WITH transport_cat AS (SELECT id FROM categories WHERE name = 'Transportation' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM transport_cat)
FROM (VALUES
    ('Gas & Fuel', '⛽', '#4ECDC4', 1),
    ('Public Transit', '🚌', '#6ED9D0', 2),
    ('Taxi & Rideshare', '🚕', '#8EE5DC', 3),
    ('Parking', '🅿️', '#AEF1E8', 4),
    ('Vehicle Maintenance', '🔧', '#CEFDF4', 5)
) AS t(name, icon, color, row_number);

-- Housing
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Housing', 'Housing and home expenses', 'EXPENSE', '🏠', '#95E1D3', 3, TRUE, TRUE, NULL);

WITH housing_cat AS (SELECT id FROM categories WHERE name = 'Housing' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM housing_cat)
FROM (VALUES
    ('Rent', '🏘️', '#95E1D3', 1),
    ('Mortgage', '🏡', '#A8E8DB', 2),
    ('Home Insurance', '🛡️', '#BBEFE3', 3),
    ('Property Tax', '📋', '#CEF6EB', 4),
    ('HOA Fees', '🏢', '#E1FDF3', 5),
    ('Utilities', '💡', '#F4FFFF', 6),
    ('Internet & Cable', '📡', '#95E1D3', 7),
    ('Home Maintenance', '🔨', '#A8E8DB', 8)
) AS t(name, icon, color, row_number);

-- Shopping
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Shopping', 'Shopping and retail', 'EXPENSE', '🛍️', '#F38181', 4, TRUE, TRUE, NULL);

WITH shopping_cat AS (SELECT id FROM categories WHERE name = 'Shopping' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM shopping_cat)
FROM (VALUES
    ('Clothing', '👔', '#F38181', 1),
    ('Electronics', '💻', '#F59393', 2),
    ('Books & Media', '📚', '#F7A5A5', 3),
    ('Hobbies', '🎨', '#F9B7B7', 4),
    ('Gifts', '🎁', '#FBC9C9', 5),
    ('Online Shopping', '📦', '#FDDBDB', 6)
) AS t(name, icon, color, row_number);

-- Entertainment
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Entertainment', 'Entertainment and leisure', 'EXPENSE', '🎭', '#AA96DA', 5, TRUE, TRUE, NULL);

WITH entertainment_cat AS (SELECT id FROM categories WHERE name = 'Entertainment' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM entertainment_cat)
FROM (VALUES
    ('Movies & Theater', '🎬', '#AA96DA', 1),
    ('Concerts & Events', '🎵', '#B8A5E0', 2),
    ('Sports & Fitness', '⚽', '#C6B4E6', 3),
    ('Streaming Services', '📺', '#D4C3EC', 4),
    ('Gaming', '🎮', '#E2D2F2', 5),
    ('Travel & Vacation', '✈️', '#F0E1F8', 6)
) AS t(name, icon, color, row_number);

-- Healthcare
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Healthcare', 'Health and medical expenses', 'EXPENSE', '🏥', '#FF6B9D', 6, TRUE, TRUE, NULL);

WITH healthcare_cat AS (SELECT id FROM categories WHERE name = 'Healthcare' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM healthcare_cat)
FROM (VALUES
    ('Doctor Visits', '👨‍⚕️', '#FF6B9D', 1),
    ('Pharmacy', '💊', '#FF82AC', 2),
    ('Dental', '🦷', '#FF99BB', 3),
    ('Vision', '👓', '#FFB0CA', 4),
    ('Health Insurance', '🏥', '#FFC7D9', 5),
    ('Gym & Fitness', '💪', '#FFDEE8', 6)
) AS t(name, icon, color, row_number);

-- Personal Care
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Personal Care', 'Personal care and grooming', 'EXPENSE', '💇', '#C7CEEA', 7, TRUE, TRUE, NULL);

WITH personal_cat AS (SELECT id FROM categories WHERE name = 'Personal Care' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM personal_cat)
FROM (VALUES
    ('Haircuts', '💈', '#C7CEEA', 1),
    ('Beauty & Spa', '💅', '#D3D9EF', 2),
    ('Toiletries', '🧴', '#DFE4F4', 3),
    ('Laundry', '🧺', '#EBEFF9', 4)
) AS t(name, icon, color, row_number);

-- Education
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Education', 'Education and learning', 'EXPENSE', '🎓', '#FFE66D', 8, TRUE, TRUE, NULL);

WITH education_cat AS (SELECT id FROM categories WHERE name = 'Education' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM education_cat)
FROM (VALUES
    ('Tuition', '📖', '#FFE66D', 1),
    ('Books & Supplies', '📚', '#FFEB85', 2),
    ('Courses & Training', '🎯', '#FFF09D', 3),
    ('Student Loans', '🎓', '#FFF5B5', 4)
) AS t(name, icon, color, row_number);

-- Bills & Utilities
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Bills & Utilities', 'Regular bills and utilities', 'EXPENSE', '📄', '#A8DADC', 9, TRUE, TRUE, NULL);

WITH bills_cat AS (SELECT id FROM categories WHERE name = 'Bills & Utilities' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM bills_cat)
FROM (VALUES
    ('Electricity', '💡', '#A8DADC', 1),
    ('Water', '💧', '#B6DFE1', 2),
    ('Gas', '🔥', '#C4E4E6', 3),
    ('Phone', '📱', '#D2E9EB', 4),
    ('Internet', '🌐', '#E0EEF0', 5),
    ('Subscriptions', '📋', '#EEF3F5', 6)
) AS t(name, icon, color, row_number);

-- Financial
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Financial', 'Financial expenses', 'EXPENSE', '💰', '#FEC8D8', 10, TRUE, TRUE, NULL);

WITH financial_cat AS (SELECT id FROM categories WHERE name = 'Financial' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM financial_cat)
FROM (VALUES
    ('Bank Fees', '🏦', '#FEC8D8', 1),
    ('Interest Charges', '📊', '#FED3E0', 2),
    ('Taxes', '📝', '#FEDEE8', 3),
    ('Insurance', '🛡️', '#FFE9F0', 4),
    ('Loans', '💳', '#FFF4F8', 5)
) AS t(name, icon, color, row_number);

-- Pets
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Pets', 'Pet care and expenses', 'EXPENSE', '🐾', '#B4E7CE', 11, TRUE, TRUE, NULL);

WITH pets_cat AS (SELECT id FROM categories WHERE name = 'Pets' AND is_system = TRUE LIMIT 1)
INSERT INTO categories (id, user_id, name, type, icon, color, display_order, is_system, is_active, parent_category_id)
SELECT uuid_generate_v4(), NULL, name, 'EXPENSE', icon, color, row_number, TRUE, TRUE, (SELECT id FROM pets_cat)
FROM (VALUES
    ('Pet Food', '🥘', '#B4E7CE', 1),
    ('Veterinary', '🏥', '#C2EBD7', 2),
    ('Grooming', '✂️', '#D0EFE0', 3),
    ('Pet Supplies', '🧸', '#DEF3E9', 4)
) AS t(name, icon, color, row_number);

-- Other Expenses
INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Other Expenses', 'Miscellaneous expenses', 'EXPENSE', '📦', '#D3D3D3', 99, TRUE, TRUE, NULL);

-- ============================================================================
-- DEFAULT INCOME CATEGORIES
-- ============================================================================

INSERT INTO categories (id, user_id, name, description, type, icon, color, display_order, is_system, is_active, parent_category_id)
VALUES
    (uuid_generate_v4(), NULL, 'Salary', 'Regular salary income', 'INCOME', '💼', '#4CAF50', 1, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Freelance', 'Freelance and contract work', 'INCOME', '💻', '#66BB6A', 2, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Business Income', 'Business and self-employment income', 'INCOME', '🏢', '#81C784', 3, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Investment Income', 'Returns from investments', 'INCOME', '📈', '#9CCC65', 4, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Rental Income', 'Income from property rentals', 'INCOME', '🏘️', '#AED581', 5, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Interest', 'Interest income', 'INCOME', '💹', '#C5E1A5', 6, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Dividends', 'Dividend income', 'INCOME', '📊', '#DCEDC8', 7, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Bonus', 'Work bonuses', 'INCOME', '🎁', '#8BC34A', 8, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Refund', 'Refunds and reimbursements', 'INCOME', '💵', '#7CB342', 9, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Gifts', 'Monetary gifts received', 'INCOME', '🎉', '#689F38', 10, TRUE, TRUE, NULL),
    (uuid_generate_v4(), NULL, 'Other Income', 'Miscellaneous income', 'INCOME', '💰', '#558B2F', 99, TRUE, TRUE, NULL);

-- ============================================================================
-- DEFAULT TAGS
-- ============================================================================

-- Note: Tags are user-specific, so we don't create default tags here.
-- Users will create their own tags as needed.

COMMENT ON TABLE categories IS 'Product categories for transactions - includes both user-specific and system-wide categories';
COMMENT ON TABLE tags IS 'User-defined tags for flexible transaction categorization';
COMMENT ON TABLE transactions IS 'Core transaction records tracking all financial movements';
COMMENT ON TABLE wallets IS 'User financial accounts and wallets';
COMMENT ON TABLE budgets IS 'User budget definitions and tracking';
COMMENT ON TABLE savings_goals IS 'User savings goals and progress tracking';
