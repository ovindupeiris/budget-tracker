# 🎯 API Examples

Real-world examples for using the Budget Tracker API.

## Table of Contents
- [Authentication](#authentication)
- [User Management](#user-management)
- [Wallet Operations](#wallet-operations)
- [Transaction Management](#transaction-management)
- [Budget Management](#budget-management)
- [Savings Goals](#savings-goals)
- [Categories & Tags](#categories--tags)

---

## Authentication

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "sarah@example.com",
    "password": "SecurePass123!",
    "firstName": "Sarah",
    "lastName": "Johnson",
    "preferredCurrency": "USD",
    "timezone": "America/New_York"
  }'
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "sarah@example.com",
    "firstName": "Sarah",
    "lastName": "Johnson",
    "status": "PENDING_VERIFICATION"
  }
}
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "sarah@example.com",
    "password": "SecurePass123!"
  }'
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "sarah@example.com",
      "firstName": "Sarah",
      "lastName": "Johnson"
    }
  }
}
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }'
```

---

## User Management

**Set JWT Token for all subsequent requests:**
```bash
export TOKEN="your_access_token_here"
```

### Get Current User Profile

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "sarah@example.com",
    "firstName": "Sarah",
    "lastName": "Johnson",
    "preferredCurrency": "USD",
    "timezone": "America/New_York",
    "preferences": {
      "language": "en",
      "dateFormat": "MM/DD/YYYY",
      "firstDayOfWeek": "SUNDAY"
    }
  }
}
```

### Update User Profile

```bash
curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Sarah",
    "lastName": "Johnson-Smith",
    "timezone": "America/Los_Angeles"
  }'
```

---

## Wallet Operations

### Create a Wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Chase Checking",
    "type": "BANK_ACCOUNT",
    "currency": "USD",
    "initialBalance": 5000.00,
    "color": "#0052CC",
    "icon": "bank"
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "wallet-id-123",
    "name": "Chase Checking",
    "type": "BANK_ACCOUNT",
    "currency": "USD",
    "balance": 5000.00,
    "color": "#0052CC",
    "icon": "bank"
  }
}
```

### Create Multiple Wallets

```bash
# Cash Wallet
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Cash",
    "type": "CASH",
    "currency": "USD",
    "initialBalance": 200.00,
    "color": "#00875A",
    "icon": "cash"
  }'

# Credit Card
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Visa Card",
    "type": "CREDIT_CARD",
    "currency": "USD",
    "initialBalance": -1500.00,
    "creditLimit": 5000.00,
    "color": "#FF5630",
    "icon": "credit-card"
  }'

# Savings Account
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Savings Account",
    "type": "SAVINGS_ACCOUNT",
    "currency": "USD",
    "initialBalance": 15000.00,
    "color": "#36B37E",
    "icon": "piggy-bank"
  }'
```

### List All Wallets

```bash
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "wallet-1",
        "name": "Chase Checking",
        "type": "BANK_ACCOUNT",
        "currency": "USD",
        "balance": 5000.00
      },
      {
        "id": "wallet-2",
        "name": "Cash",
        "type": "CASH",
        "currency": "USD",
        "balance": 200.00
      },
      {
        "id": "wallet-3",
        "name": "Visa Card",
        "type": "CREDIT_CARD",
        "currency": "USD",
        "balance": -1500.00,
        "creditLimit": 5000.00
      }
    ],
    "totalBalance": 3700.00,
    "totalBalanceByC currency": {
      "USD": 3700.00
    }
  }
}
```

---

## Transaction Management

### Create an Expense Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-1",
    "type": "EXPENSE",
    "amount": 85.50,
    "currency": "USD",
    "description": "Grocery shopping at Whole Foods",
    "transactionDate": "2025-10-07",
    "categoryId": "category-food",
    "tags": ["groceries", "weekly"],
    "notes": "Weekly grocery run"
  }'
```

### Create an Income Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-1",
    "type": "INCOME",
    "amount": 5000.00,
    "currency": "USD",
    "description": "Monthly Salary",
    "transactionDate": "2025-10-01",
    "categoryId": "category-salary"
  }'
```

### Create a Transfer Between Wallets

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-1",
    "type": "TRANSFER",
    "amount": 1000.00,
    "currency": "USD",
    "description": "Transfer to savings",
    "transactionDate": "2025-10-07",
    "toWalletId": "wallet-3"
  }'
```

### Get Transactions with Filtering

```bash
# Get all transactions
curl -X GET "http://localhost:8080/api/transactions?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Filter by date range
curl -X GET "http://localhost:8080/api/transactions?startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer $TOKEN"

# Filter by type
curl -X GET "http://localhost:8080/api/transactions?type=EXPENSE" \
  -H "Authorization: Bearer $TOKEN"

# Filter by wallet
curl -X GET "http://localhost:8080/api/transactions?walletId=wallet-1" \
  -H "Authorization: Bearer $TOKEN"

# Search transactions
curl -X GET "http://localhost:8080/api/transactions/search?query=grocery" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Transaction Statistics

```bash
curl -X GET "http://localhost:8080/api/transactions/statistics?startDate=2025-10-01&endDate=2025-10-31" \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalIncome": 5000.00,
    "totalExpenses": 2345.67,
    "netIncome": 2654.33,
    "transactionCount": 45,
    "averageExpense": 52.13,
    "topCategories": [
      {
        "categoryName": "Food & Dining",
        "amount": 567.89,
        "percentage": 24.2
      },
      {
        "categoryName": "Transportation",
        "amount": 345.50,
        "percentage": 14.7
      }
    ]
  }
}
```

---

## Budget Management

### Create a Monthly Budget

```bash
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "October 2025 Budget",
    "amount": 3000.00,
    "currency": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "category-food",
    "alertThreshold": 80,
    "walletIds": ["wallet-1", "wallet-2"]
  }'
```

### Create Category-Specific Budgets

```bash
# Food Budget
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Food Budget - October",
    "amount": 600.00,
    "currency": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "categoryId": "category-food",
    "alertThreshold": 90
  }'

# Transportation Budget
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Transportation Budget - October",
    "amount": 300.00,
    "currency": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "categoryId": "category-transport",
    "alertThreshold": 85
  }'
```

### Get Budget Progress

```bash
curl -X GET "http://localhost:8080/api/budgets/budget-id-123/progress" \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "budgetId": "budget-id-123",
    "budgetAmount": 600.00,
    "spent": 456.78,
    "remaining": 143.22,
    "percentageUsed": 76.13,
    "daysRemaining": 15,
    "isOverBudget": false,
    "alertTriggered": false
  }
}
```

---

## Savings Goals

### Create a Savings Goal

```bash
curl -X POST http://localhost:8080/api/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currency": "USD",
    "currentAmount": 0.00,
    "targetDate": "2026-01-01",
    "walletId": "wallet-3",
    "color": "#FF5630",
    "icon": "target"
  }'
```

### Add Contribution to Goal

```bash
curl -X POST "http://localhost:8080/api/goals/goal-id-123/contribute" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "amount": 500.00
  }'
```

### Get Goal Progress

```bash
curl -X GET "http://localhost:8080/api/goals/goal-id-123" \
  -H "Authorization: Bearer $TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "goal-id-123",
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currentAmount": 2500.00,
    "percentageComplete": 25.0,
    "remaining": 7500.00,
    "targetDate": "2026-01-01",
    "daysRemaining": 87,
    "monthlyTargetContribution": 862.07,
    "onTrack": true
  }
}
```

---

## Categories & Tags

### List All Categories

```bash
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN"
```

### Create Custom Category

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Pet Care",
    "type": "EXPENSE",
    "color": "#FFC400",
    "icon": "paw",
    "parentCategoryId": null
  }'
```

### Create and Assign Tags

```bash
# Create a transaction with tags
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-1",
    "type": "EXPENSE",
    "amount": 45.00,
    "description": "Dog food",
    "transactionDate": "2025-10-07",
    "categoryId": "category-pet-care",
    "tags": ["pets", "monthly", "essential"]
  }'
```

---

## Complete Use Case: Monthly Budget Setup

```bash
#!/bin/bash

TOKEN="your_access_token_here"
API_URL="http://localhost:8080/api"

# 1. Create wallets
CHECKING=$(curl -s -X POST "$API_URL/wallets" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Checking","type":"BANK_ACCOUNT","currency":"USD","initialBalance":5000}' \
  | jq -r '.data.id')

# 2. Create monthly budget
curl -X POST "$API_URL/budgets" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"October Budget",
    "amount":3000,
    "currency":"USD",
    "period":"MONTHLY",
    "startDate":"2025-10-01",
    "walletIds":["'$CHECKING'"]
  }'

# 3. Add transactions
curl -X POST "$API_URL/transactions" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId":"'$CHECKING'",
    "type":"EXPENSE",
    "amount":85.50,
    "description":"Groceries",
    "transactionDate":"2025-10-07"
  }'

# 4. Check budget progress
curl -X GET "$API_URL/budgets?period=CURRENT" \
  -H "Authorization: Bearer $TOKEN"

echo "Monthly budget setup complete!"
```

---

## Error Handling Examples

### 401 Unauthorized

```bash
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer invalid_token"
```

**Response (401)**:
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token",
    "timestamp": "2025-10-07T10:30:00Z"
  }
}
```

### 400 Bad Request

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-1",
    "type": "EXPENSE",
    "amount": -50.00
  }'
```

**Response (400)**:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "fields": {
      "amount": "Amount must be positive",
      "description": "Description is required"
    }
  }
}
```

---

For complete API reference, see [API_REFERENCE.md](API_REFERENCE.md)
