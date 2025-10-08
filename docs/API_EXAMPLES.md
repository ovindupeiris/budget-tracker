# 💡 Budget Tracker API Examples

Real-world usage examples and common workflows for the Budget Tracker API.

## Table of Contents

- [Getting Started](#getting-started)
- [Authentication Workflows](#authentication-workflows)
- [Wallet Management](#wallet-management)
- [Transaction Workflows](#transaction-workflows)
- [Budget Management](#budget-management)
- [Reports & Analytics](#reports--analytics)
- [Common Use Cases](#common-use-cases)
- [Error Handling](#error-handling)

---

## Getting Started

### Base URL

```
http://localhost:8080/api
```

### Prerequisites

- Budget Tracker backend running locally
- curl, Postman, or any HTTP client
- Valid user credentials (or create a new account)

---

## Authentication Workflows

### Example 1: Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "currencyCode": "USD",
    "timezone": "America/New_York"
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "status": "ACTIVE",
    "roles": ["USER"],
    "emailVerified": false,
    "currencyCode": "USD",
    "timezone": "America/New_York",
    "subscriptionTier": "FREE",
    "createdAt": "2025-10-08T10:30:00"
  }
}
```

### Example 2: Login and Get Access Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "emailOrUsername": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

**Response:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "email": "john.doe@example.com",
      "username": "johndoe",
      "firstName": "John",
      "lastName": "Doe"
    }
  }
}
```

**Save the access token for subsequent requests:**

```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 3: Check Email Availability Before Registration

```bash
curl -X GET 'http://localhost:8080/api/auth/check-email?email=newuser@example.com'
```

**Response:**

```json
{
  "success": true,
  "data": true
}
```

Returns `true` if available, `false` if taken.

---

## Wallet Management

### Example 4: Create a Checking Account

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Main Checking",
    "description": "Primary checking account",
    "type": "CHECKING",
    "currencyCode": "USD",
    "initialBalance": 2500.00,
    "icon": "bank",
    "color": "#4CAF50",
    "isDefault": true
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "wallet-uuid-1",
    "name": "Main Checking",
    "type": "CHECKING",
    "currencyCode": "USD",
    "balance": 2500.00,
    "initialBalance": 2500.00,
    "isDefault": true,
    "createdAt": "2025-10-08T10:30:00"
  }
}
```

### Example 5: Create a Credit Card Wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Visa Credit Card",
    "type": "CREDIT_CARD",
    "currencyCode": "USD",
    "initialBalance": 0,
    "creditLimit": 5000.00,
    "billingCycleDay": 1,
    "paymentDueDay": 25,
    "icon": "credit-card",
    "color": "#2196F3"
  }'
```

### Example 6: Get All Wallets

```bash
curl -X GET http://localhost:8080/api/wallets \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "wallet-uuid-1",
      "name": "Main Checking",
      "type": "CHECKING",
      "currencyCode": "USD",
      "balance": 2500.00,
      "isDefault": true
    },
    {
      "id": "wallet-uuid-2",
      "name": "Visa Credit Card",
      "type": "CREDIT_CARD",
      "currencyCode": "USD",
      "balance": -350.00,
      "creditLimit": 5000.00,
      "availableCredit": 4650.00
    }
  ]
}
```

### Example 7: Get Total Balance Across All Wallets

```bash
curl -X GET http://localhost:8080/api/wallets/balance/total \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": 2150.00
}
```

---

## Transaction Workflows

### Example 8: Record an Expense (Grocery Shopping)

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-uuid-1",
    "categoryId": "groceries-category-uuid",
    "type": "EXPENSE",
    "amount": 125.50,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Weekly grocery shopping",
    "notes": "Whole Foods Market - bought organic produce",
    "merchantName": "Whole Foods",
    "location": "New York, NY"
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": "transaction-uuid-1",
    "walletId": "wallet-uuid-1",
    "walletName": "Main Checking",
    "categoryId": "groceries-category-uuid",
    "categoryName": "Groceries",
    "type": "EXPENSE",
    "amount": 125.50,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Weekly grocery shopping",
    "merchantName": "Whole Foods",
    "status": "COMPLETED",
    "createdAt": "2025-10-08T10:30:00"
  }
}
```

### Example 9: Record Income (Salary)

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-uuid-1",
    "categoryId": "salary-category-uuid",
    "type": "INCOME",
    "amount": 5000.00,
    "currencyCode": "USD",
    "transactionDate": "2025-10-01",
    "description": "Monthly salary",
    "merchantName": "Acme Corporation"
  }'
```

### Example 10: Transfer Between Wallets

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "checking-wallet-uuid",
    "toWalletId": "savings-wallet-uuid",
    "type": "TRANSFER",
    "amount": 500.00,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Transfer to savings",
    "transferFee": 0.00
  }'
```

### Example 11: Get Transactions for Current Month

```bash
curl -X GET 'http://localhost:8080/api/transactions/date-range?startDate=2025-10-01&endDate=2025-10-31&page=0&size=20&sort=transactionDate,desc' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "transaction-uuid-1",
        "amount": 125.50,
        "type": "EXPENSE",
        "categoryName": "Groceries",
        "walletName": "Main Checking",
        "transactionDate": "2025-10-08",
        "description": "Weekly grocery shopping"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 42,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

### Example 12: Search Transactions

```bash
curl -X GET 'http://localhost:8080/api/transactions/search?query=grocery&page=0&size=10' \
  -H 'Authorization: Bearer '$TOKEN
```

### Example 13: Get Total Income and Expenses for Month

**Get Total Income:**

```bash
curl -X GET 'http://localhost:8080/api/transactions/stats/income?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": 5000.00
}
```

**Get Total Expenses:**

```bash
curl -X GET 'http://localhost:8080/api/transactions/stats/expenses?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": 3250.00
}
```

---

## Budget Management

### Example 14: Create a Monthly Budget

```bash
curl -X POST http://localhost:8080/api/budgets \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Monthly Groceries Budget",
    "description": "Budget for food and household items",
    "amount": 600.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "groceries-category-uuid",
    "alertThreshold": 80.00,
    "alertEnabled": true,
    "rolloverEnabled": false
  }'
```

**Response:**

```json
{
  "success": true,
  "message": "Budget created successfully",
  "data": {
    "id": "budget-uuid-1",
    "name": "Monthly Groceries Budget",
    "categoryName": "Groceries",
    "amount": 600.00,
    "spent": 0.00,
    "remaining": 600.00,
    "percentageUsed": 0.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "status": "ACTIVE",
    "alertThreshold": 80.00,
    "alertEnabled": true,
    "createdAt": "2025-10-01T00:00:00"
  }
}
```

### Example 15: Get All Active Budgets

```bash
curl -X GET http://localhost:8080/api/budgets/active \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "budget-uuid-1",
      "name": "Monthly Groceries Budget",
      "categoryName": "Groceries",
      "amount": 600.00,
      "spent": 425.50,
      "remaining": 174.50,
      "percentageUsed": 70.92,
      "status": "ACTIVE"
    },
    {
      "id": "budget-uuid-2",
      "name": "Entertainment Budget",
      "categoryName": "Entertainment",
      "amount": 200.00,
      "spent": 175.00,
      "remaining": 25.00,
      "percentageUsed": 87.50,
      "status": "ACTIVE"
    }
  ]
}
```

### Example 16: Get Budgets Exceeding Alert Threshold

```bash
curl -X GET http://localhost:8080/api/budgets/alerts \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "budget-uuid-2",
      "name": "Entertainment Budget",
      "amount": 200.00,
      "spent": 175.00,
      "remaining": 25.00,
      "percentageUsed": 87.50,
      "alertThreshold": 80.00,
      "alertEnabled": true,
      "alertSent": true
    }
  ]
}
```

### Example 17: Pause a Budget

```bash
curl -X POST http://localhost:8080/api/budgets/budget-uuid-1/pause \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "message": "Budget paused successfully"
}
```

---

## Reports & Analytics

### Example 18: Get Dashboard Summary

```bash
curl -X GET 'http://localhost:8080/api/reports/dashboard?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": {
    "totalIncome": 5000.00,
    "totalExpenses": 3250.00,
    "netSavings": 1750.00,
    "totalBalance": 8500.00,
    "budgetUtilization": 72.50,
    "transactionCount": 48,
    "topSpendingCategory": "Groceries",
    "topSpendingAmount": 425.50,
    "averageDailyExpense": 104.84
  }
}
```

### Example 19: Get Spending Breakdown by Category

```bash
curl -X GET 'http://localhost:8080/api/reports/spending-by-category?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": {
    "Groceries": 425.50,
    "Transportation": 350.00,
    "Entertainment": 175.00,
    "Utilities": 220.00,
    "Dining Out": 380.00,
    "Shopping": 425.00,
    "Healthcare": 150.00,
    "Other": 124.50
  }
}
```

### Example 20: Get Income Breakdown by Category

```bash
curl -X GET 'http://localhost:8080/api/reports/income-by-category?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": {
    "Salary": 4500.00,
    "Freelance": 300.00,
    "Investments": 150.00,
    "Other": 50.00
  }
}
```

### Example 21: Get Monthly Trends (Last 6 Months)

```bash
curl -X GET 'http://localhost:8080/api/reports/trends?months=6' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": {
    "labels": ["2025-05", "2025-06", "2025-07", "2025-08", "2025-09", "2025-10"],
    "income": [5000.00, 5200.00, 4800.00, 5500.00, 5000.00, 5000.00],
    "expenses": [3200.00, 3500.00, 3100.00, 3800.00, 3400.00, 3250.00],
    "net": [1800.00, 1700.00, 1700.00, 1700.00, 1600.00, 1750.00]
  }
}
```

### Example 22: Get Recent Transactions for Dashboard

```bash
curl -X GET 'http://localhost:8080/api/reports/recent-transactions?limit=5' \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "transaction-uuid-1",
      "amount": 125.50,
      "type": "EXPENSE",
      "category": "Groceries",
      "wallet": "Main Checking",
      "date": "2025-10-08",
      "description": "Weekly grocery shopping"
    },
    {
      "id": "transaction-uuid-2",
      "amount": 45.00,
      "type": "EXPENSE",
      "category": "Transportation",
      "wallet": "Main Checking",
      "date": "2025-10-07",
      "description": "Gas"
    }
  ]
}
```

---

## Common Use Cases

### Use Case 1: Complete Monthly Budget Setup

**Step 1: Create categories**

```bash
# Create Groceries category
curl -X POST http://localhost:8080/api/categories \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Groceries",
    "type": "EXPENSE",
    "icon": "shopping-cart",
    "color": "#FF5722"
  }'

# Create Entertainment category
curl -X POST http://localhost:8080/api/categories \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Entertainment",
    "type": "EXPENSE",
    "icon": "movie",
    "color": "#9C27B0"
  }'
```

**Step 2: Create budgets for each category**

```bash
# Groceries budget
curl -X POST http://localhost:8080/api/budgets \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Monthly Groceries",
    "amount": 600.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "groceries-category-uuid",
    "alertThreshold": 80.00,
    "alertEnabled": true
  }'

# Entertainment budget
curl -X POST http://localhost:8080/api/budgets \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Monthly Entertainment",
    "amount": 200.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "entertainment-category-uuid",
    "alertThreshold": 75.00,
    "alertEnabled": true
  }'
```

**Step 3: Track your spending**

```bash
# Record a grocery expense
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-uuid-1",
    "categoryId": "groceries-category-uuid",
    "type": "EXPENSE",
    "amount": 85.50,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Grocery shopping"
  }'
```

**Step 4: Monitor budget progress**

```bash
# Check budget status
curl -X GET http://localhost:8080/api/budgets/active \
  -H 'Authorization: Bearer '$TOKEN
```

### Use Case 2: Track Multi-Currency Transactions

**Scenario:** You're traveling abroad and making purchases in EUR

```bash
# Record expense in EUR
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "usd-wallet-uuid",
    "categoryId": "dining-category-uuid",
    "type": "EXPENSE",
    "amount": 45.00,
    "currencyCode": "EUR",
    "exchangeRate": 1.10,
    "transactionDate": "2025-10-08",
    "description": "Dinner in Paris",
    "location": "Paris, France"
  }'
```

The transaction will be recorded in EUR but converted to your wallet's base currency (USD) using the exchange rate.

### Use Case 3: End-of-Month Financial Review

```bash
# 1. Get dashboard summary
curl -X GET 'http://localhost:8080/api/reports/dashboard?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN

# 2. Get spending by category
curl -X GET 'http://localhost:8080/api/reports/spending-by-category?startDate=2025-10-01&endDate=2025-10-31' \
  -H 'Authorization: Bearer '$TOKEN

# 3. Check which budgets were exceeded
curl -X GET http://localhost:8080/api/budgets/exceeded \
  -H 'Authorization: Bearer '$TOKEN

# 4. Get monthly trends
curl -X GET 'http://localhost:8080/api/reports/trends?months=6' \
  -H 'Authorization: Bearer '$TOKEN
```

---

## Error Handling

### Example: Invalid Request (Missing Required Field)

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-uuid-1",
    "type": "EXPENSE",
    "currencyCode": "USD"
  }'
```

**Response:** `400 Bad Request`

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "amount",
      "message": "Amount is required"
    },
    {
      "field": "transactionDate",
      "message": "Transaction date is required"
    }
  ],
  "timestamp": "2025-10-08T10:30:00"
}
```

### Example: Unauthorized Request

```bash
curl -X GET http://localhost:8080/api/wallets
```

**Response:** `401 Unauthorized`

```json
{
  "success": false,
  "message": "Full authentication is required to access this resource",
  "timestamp": "2025-10-08T10:30:00"
}
```

### Example: Resource Not Found

```bash
curl -X GET http://localhost:8080/api/wallets/invalid-uuid \
  -H 'Authorization: Bearer '$TOKEN
```

**Response:** `404 Not Found`

```json
{
  "success": false,
  "message": "Wallet not found with id: invalid-uuid",
  "timestamp": "2025-10-08T10:30:00"
}
```

---

## Tips & Best Practices

### 1. Save Your Access Token

After login, save the access token as an environment variable:

```bash
export TOKEN="your_access_token_here"
```

Then use it in all subsequent requests:

```bash
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN"
```

### 2. Use Proper Date Formats

Always use ISO 8601 date format (`YYYY-MM-DD`):

✅ Good: `"2025-10-08"`
❌ Bad: `"10/08/2025"` or `"08-10-2025"`

### 3. Handle Pagination Properly

For large datasets, use pagination parameters:

```bash
curl -X GET 'http://localhost:8080/api/transactions?page=0&size=50&sort=transactionDate,desc' \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Set Alert Thresholds on Budgets

Always enable alerts and set reasonable thresholds (e.g., 80%) to get notified before overspending:

```json
{
  "alertThreshold": 80.00,
  "alertEnabled": true
}
```

### 5. Use Descriptive Transaction Notes

Add detailed notes to transactions for better tracking:

```json
{
  "description": "Weekly grocery shopping",
  "notes": "Organic produce, toiletries, and household items",
  "merchantName": "Whole Foods",
  "location": "New York, NY"
}
```

---

## Testing with Postman

### Import Collection

You can import these examples into Postman:

1. Create a new collection called "Budget Tracker API"
2. Add environment variables:
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: Your access token
3. Use `{{baseUrl}}` and `{{token}}` in your requests

### Example Postman Request

```
GET {{baseUrl}}/wallets
Authorization: Bearer {{token}}
```

---

## Additional Resources

- **API Reference:** [API_REFERENCE.md](API_REFERENCE.md)
- **Deployment Guide:** [DEPLOYMENT.md](DEPLOYMENT.md)
- **Interactive API Docs:** http://localhost:8080/swagger-ui.html
- **GitHub Repository:** https://github.com/yourusername/budget-tracker

---

**Built with ❤️ using Spring Boot 3.2.1 | Happy Tracking! 💰**
