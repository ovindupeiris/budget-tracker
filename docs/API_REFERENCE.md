# 📖 Budget Tracker API Reference

Complete API reference for the Budget Tracker backend API (v1.0).

## Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Common Response Structure](#common-response-structure)
- [Pagination](#pagination)
- [Error Handling](#error-handling)
- [API Endpoints](#api-endpoints)
  - [1. Authentication API](#1-authentication-api)
  - [2. User Management API](#2-user-management-api)
  - [3. Wallets API](#3-wallets-api)
  - [4. Categories API](#4-categories-api)
  - [5. Transactions API](#5-transactions-api)
  - [6. Budgets API](#6-budgets-api)
  - [7. Reports & Analytics API](#7-reports--analytics-api)

---

## Overview

**Base URL:** `http://localhost:8080/api`
**Content Type:** `application/json`
**API Version:** v1.0

The Budget Tracker API is organized around REST. It accepts JSON-encoded request bodies and returns JSON-encoded responses. Standard HTTP response codes and authentication using JWT Bearer tokens.

### Interactive Documentation

For live API testing and exploration, visit the Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

---

## Authentication

Most API endpoints require authentication using JWT (JSON Web Tokens).

### Obtaining Access Tokens

**Step 1: Register or Login**

```bash
# Register a new user
POST /api/auth/register

# Or login with existing credentials
POST /api/auth/login
```

**Step 2: Use the Access Token**

Include the access token in the `Authorization` header:

```
Authorization: Bearer <your_access_token>
```

### Token Lifecycle

- **Access Token:** Valid for 24 hours (configurable)
- **Refresh Token:** Used to obtain new access tokens
- Tokens are returned in the `AuthResponse` from login/register endpoints

### Public Endpoints (No Authentication Required)

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/verify-email`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/check-email`
- `GET /api/auth/check-username`

---

## Common Response Structure

All API responses follow a consistent structure:

### Success Response

```json
{
  "success": true,
  "message": "Optional success message",
  "data": {
    // Response data here
  }
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "errors": [
    {
      "field": "fieldName",
      "message": "Validation error message"
    }
  ],
  "timestamp": "2025-10-08T10:30:00"
}
```

---

## Pagination

Endpoints returning lists support pagination using query parameters:

### Query Parameters

- `page` (integer, default: 0) - Page number (0-indexed)
- `size` (integer, default: 20) - Number of items per page
- `sort` (string, optional) - Sort field and direction (e.g., `"createdAt,desc"`)

### Paginated Response Structure

```json
{
  "success": true,
  "data": {
    "content": [ /* array of items */ ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## Error Handling

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request succeeded |
| 201 Created | Resource created successfully |
| 400 Bad Request | Invalid request or validation error |
| 401 Unauthorized | Missing or invalid authentication |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource not found |
| 500 Internal Server Error | Server error |

---

## API Endpoints

---

## 1. Authentication API

**Base Path:** `/api/auth`

### 1.1 Register New User

Create a new user account.

**Endpoint:** `POST /api/auth/register`
**Authentication:** Not required

**Request Body:**

```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "currencyCode": "USD",
  "timezone": "America/New_York",
  "locale": "en-US"
}
```

**Required Fields:** `email`, `password`

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "status": "ACTIVE",
    "roles": ["USER"],
    "emailVerified": false,
    "currencyCode": "USD",
    "timezone": "America/New_York",
    "locale": "en-US",
    "subscriptionTier": "FREE",
    "createdAt": "2025-10-08T10:30:00"
  }
}
```

### 1.2 Login

Authenticate and receive access tokens.

**Endpoint:** `POST /api/auth/login`
**Authentication:** Not required

**Request Body:**

```json
{
  "emailOrUsername": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "email": "user@example.com",
      "username": "johndoe",
      "firstName": "John",
      "lastName": "Doe"
    }
  }
}
```

### 1.3 Verify Email

Verify user's email address using token from email.

**Endpoint:** `GET /api/auth/verify-email?token={token}`
**Authentication:** Not required

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

### 1.4 Forgot Password

Initiate password reset process.

**Endpoint:** `POST /api/auth/forgot-password?email={email}`
**Authentication:** Not required

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Password reset email sent"
}
```

### 1.5 Reset Password

Reset password using token from email.

**Endpoint:** `POST /api/auth/reset-password?token={token}&newPassword={newPassword}`
**Authentication:** Not required

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Password reset successfully"
}
```

### 1.6 Check Email Availability

Check if an email is available for registration.

**Endpoint:** `GET /api/auth/check-email?email={email}`
**Authentication:** Not required

**Response:** `200 OK`

```json
{
  "success": true,
  "data": true
}
```

Returns `true` if available, `false` if taken.

### 1.7 Check Username Availability

Check if a username is available for registration.

**Endpoint:** `GET /api/auth/check-username?username={username}`
**Authentication:** Not required

**Response:** `200 OK`

```json
{
  "success": true,
  "data": true
}
```

---

## 2. User Management API

**Base Path:** `/api/users`
**Authentication:** Required

### 2.1 Get Current User Profile

Get authenticated user's profile information.

**Endpoint:** `GET /api/users/me`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profilePictureUrl": "https://...",
    "status": "ACTIVE",
    "roles": ["USER"],
    "emailVerified": true,
    "timezone": "America/New_York",
    "locale": "en-US",
    "currencyCode": "USD",
    "subscriptionTier": "FREE",
    "subscriptionExpiresAt": null,
    "createdAt": "2025-10-08T10:30:00",
    "lastLoginAt": "2025-10-08T15:45:00"
  }
}
```

### 2.2 Update User Profile

Update authenticated user's profile.

**Endpoint:** `PUT /api/users/me`

**Request Body:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "timezone": "America/New_York",
  "locale": "en-US",
  "currencyCode": "USD"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    // UserResponse object
  }
}
```

### 2.3 Update Password

Change user's password.

**Endpoint:** `PUT /api/users/me/password?oldPassword={old}&newPassword={new}`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Password updated successfully"
}
```

### 2.4 Deactivate Account

Temporarily deactivate user account.

**Endpoint:** `POST /api/users/me/deactivate`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Account deactivated successfully"
}
```

### 2.5 Delete Account

Permanently delete user account and all associated data.

**Endpoint:** `DELETE /api/users/me`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

---

## 3. Wallets API

**Base Path:** `/api/wallets`
**Authentication:** Required

### 3.1 Create Wallet

Create a new wallet/account.

**Endpoint:** `POST /api/wallets`

**Request Body:**

```json
{
  "name": "Main Checking",
  "description": "Primary checking account",
  "type": "CHECKING",
  "currencyCode": "USD",
  "initialBalance": 1000.00,
  "icon": "bank",
  "color": "#4CAF50",
  "displayOrder": 1,
  "isDefault": true,
  "excludeFromTotals": false,
  "creditLimit": null,
  "billingCycleDay": null,
  "paymentDueDay": null
}
```

**Wallet Types:** `CASH`, `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `DEBIT_CARD`, `INVESTMENT`, `LOAN`, `MORTGAGE`, `CRYPTO`, `E_WALLET`, `PREPAID`, `OTHER`

**Required Fields:** `name`, `type`, `currencyCode`

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "wallet-uuid",
    "name": "Main Checking",
    "description": "Primary checking account",
    "type": "CHECKING",
    "currencyCode": "USD",
    "balance": 1000.00,
    "initialBalance": 1000.00,
    "icon": "bank",
    "color": "#4CAF50",
    "displayOrder": 1,
    "isDefault": true,
    "isArchived": false,
    "isShared": false,
    "excludeFromTotals": false,
    "creditLimit": null,
    "availableCredit": null,
    "syncEnabled": false,
    "lastSyncedAt": null,
    "createdAt": "2025-10-08T10:30:00",
    "updatedAt": "2025-10-08T10:30:00"
  }
}
```

### 3.2 Get All Wallets

Get all wallets for authenticated user.

**Endpoint:** `GET /api/wallets?includeArchived=false`

**Query Parameters:**
- `includeArchived` (boolean, optional, default: false)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    {
      // WalletResponse objects
    }
  ]
}
```

### 3.3 Get Wallet by ID

Get specific wallet details.

**Endpoint:** `GET /api/wallets/{walletId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // WalletResponse object
  }
}
```

### 3.4 Update Wallet

Update wallet details.

**Endpoint:** `PUT /api/wallets/{walletId}`

**Request Body:** Wallet object with fields to update

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Wallet updated successfully",
  "data": {
    // Updated WalletResponse object
  }
}
```

### 3.5 Delete Wallet

Delete a wallet.

**Endpoint:** `DELETE /api/wallets/{walletId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Wallet deleted successfully"
}
```

### 3.6 Set Default Wallet

Mark a wallet as the default.

**Endpoint:** `PUT /api/wallets/{walletId}/set-default`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Default wallet set",
  "data": {
    // WalletResponse object
  }
}
```

### 3.7 Archive Wallet

Archive a wallet (hide from active view).

**Endpoint:** `PUT /api/wallets/{walletId}/archive`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Wallet archived"
}
```

### 3.8 Unarchive Wallet

Unarchive a wallet.

**Endpoint:** `PUT /api/wallets/{walletId}/unarchive`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Wallet unarchived"
}
```

### 3.9 Get Total Balance

Get total balance across all wallets.

**Endpoint:** `GET /api/wallets/balance/total?currencyCode={currency}`

**Query Parameters:**
- `currencyCode` (string, optional): Filter by currency

**Response:** `200 OK`

```json
{
  "success": true,
  "data": 5000.00
}
```

---

## 4. Categories API

**Base Path:** `/api/categories`
**Authentication:** Required

### 4.1 Get All Categories

Get all categories for current user.

**Endpoint:** `GET /api/categories`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    {
      "id": "category-uuid",
      "name": "Groceries",
      "description": "Food and household items",
      "type": "EXPENSE",
      "icon": "shopping-cart",
      "color": "#FF5722",
      "displayOrder": 1,
      "parentCategoryId": null,
      "parentCategoryName": null,
      "isSystem": false,
      "isActive": true,
      "createdAt": "2025-10-08T10:30:00"
    }
  ]
}
```

**Category Types:** `INCOME`, `EXPENSE`, `BOTH`

### 4.2 Get Categories by Type

Get categories filtered by type.

**Endpoint:** `GET /api/categories/type/{type}`

**Path Parameters:**
- `type`: `INCOME`, `EXPENSE`, or `BOTH`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of CategoryResponse objects
  ]
}
```

### 4.3 Get Parent Categories

Get only parent categories (no subcategories).

**Endpoint:** `GET /api/categories/parents`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of parent CategoryResponse objects
  ]
}
```

### 4.4 Get Category by ID

Get specific category details.

**Endpoint:** `GET /api/categories/{categoryId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // CategoryResponse object
  }
}
```

### 4.5 Get Subcategories

Get all subcategories of a parent category.

**Endpoint:** `GET /api/categories/{categoryId}/subcategories`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of subcategory CategoryResponse objects
  ]
}
```

### 4.6 Create Category

Create a new category.

**Endpoint:** `POST /api/categories`

**Request Body:**

```json
{
  "name": "Groceries",
  "description": "Food and household items",
  "type": "EXPENSE",
  "icon": "shopping-cart",
  "color": "#FF5722",
  "displayOrder": 1,
  "parentCategoryId": null,
  "isActive": true
}
```

**Required Fields:** `name`, `type`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    // CategoryResponse object
  }
}
```

### 4.7 Update Category

Update category details.

**Endpoint:** `PUT /api/categories/{categoryId}`

**Request Body:** Same as create

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Category updated successfully",
  "data": {
    // Updated CategoryResponse object
  }
}
```

### 4.8 Delete Category

Delete a category.

**Endpoint:** `DELETE /api/categories/{categoryId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Category deleted successfully"
}
```

---

## 5. Transactions API

**Base Path:** `/api/transactions`
**Authentication:** Required

### 5.1 Create Transaction

Create a new transaction.

**Endpoint:** `POST /api/transactions`

**Request Body:**

```json
{
  "walletId": "wallet-uuid",
  "categoryId": "category-uuid",
  "type": "EXPENSE",
  "amount": 45.99,
  "currencyCode": "USD",
  "exchangeRate": 1.0,
  "transactionDate": "2025-10-08",
  "description": "Grocery shopping",
  "notes": "Weekly groceries at Whole Foods",
  "merchantName": "Whole Foods",
  "location": "New York, NY",
  "toWalletId": null,
  "transferFee": null,
  "tagIds": [],
  "isRecurring": false,
  "recurringTemplateId": null
}
```

**Transaction Types:** `INCOME`, `EXPENSE`, `TRANSFER`, `INVESTMENT`, `DIVIDEND`, `INTEREST`, `FEE`, `REFUND`, `ADJUSTMENT`, `LOAN_PAYMENT`, `LOAN_DISBURSEMENT`

**Required Fields:** `walletId`, `type`, `amount`, `currencyCode`, `transactionDate`

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": "transaction-uuid",
    "walletId": "wallet-uuid",
    "walletName": "Main Checking",
    "categoryId": "category-uuid",
    "categoryName": "Groceries",
    "type": "EXPENSE",
    "amount": 45.99,
    "currencyCode": "USD",
    "exchangeRate": 1.0,
    "amountInWalletCurrency": 45.99,
    "transactionDate": "2025-10-08",
    "description": "Grocery shopping",
    "notes": "Weekly groceries at Whole Foods",
    "merchantName": "Whole Foods",
    "location": "New York, NY",
    "status": "COMPLETED",
    "isReconciled": false,
    "hasAttachments": false,
    "tags": [],
    "createdAt": "2025-10-08T10:30:00",
    "updatedAt": "2025-10-08T10:30:00"
  }
}
```

### 5.2 Get User Transactions

Get all transactions with pagination.

**Endpoint:** `GET /api/transactions?page=0&size=20&sort=transactionDate,desc`

**Query Parameters:**
- `page` (integer, default: 0)
- `size` (integer, default: 20)
- `sort` (string, optional)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "content": [
      // Array of TransactionResponse objects
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### 5.3 Get Transaction by ID

Get specific transaction details.

**Endpoint:** `GET /api/transactions/{transactionId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // TransactionResponse object
  }
}
```

### 5.4 Get Transactions by Date Range

Get transactions within a date range.

**Endpoint:** `GET /api/transactions/date-range?startDate=2025-10-01&endDate=2025-10-31`

**Query Parameters:**
- `startDate` (date, required, ISO format: YYYY-MM-DD)
- `endDate` (date, required, ISO format: YYYY-MM-DD)
- Pagination parameters: `page`, `size`, `sort`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // Paginated TransactionResponse
  }
}
```

### 5.5 Search Transactions

Search transactions by query string.

**Endpoint:** `GET /api/transactions/search?query=groceries`

**Query Parameters:**
- `query` (string, required)
- Pagination parameters: `page`, `size`, `sort`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // Paginated TransactionResponse
  }
}
```

### 5.6 Update Transaction

Update transaction details.

**Endpoint:** `PUT /api/transactions/{transactionId}`

**Request Body:** Transaction object with fields to update

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Transaction updated successfully",
  "data": {
    // Updated TransactionResponse object
  }
}
```

### 5.7 Delete Transaction

Delete a transaction.

**Endpoint:** `DELETE /api/transactions/{transactionId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Transaction deleted successfully"
}
```

### 5.8 Reconcile Transaction

Mark transaction as reconciled.

**Endpoint:** `PUT /api/transactions/{transactionId}/reconcile`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Transaction reconciled"
}
```

### 5.9 Get Total Income

Calculate total income for a period.

**Endpoint:** `GET /api/transactions/stats/income?startDate=2025-10-01&endDate=2025-10-31`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": 5000.00
}
```

### 5.10 Get Total Expenses

Calculate total expenses for a period.

**Endpoint:** `GET /api/transactions/stats/expenses?startDate=2025-10-01&endDate=2025-10-31`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": 3500.00
}
```

---

## 6. Budgets API

**Base Path:** `/api/budgets`
**Authentication:** Required

### 6.1 Get All Budgets

Get all budgets for current user.

**Endpoint:** `GET /api/budgets`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    {
      "id": "budget-uuid",
      "name": "Monthly Groceries",
      "description": "Food budget for the month",
      "categoryId": "category-uuid",
      "categoryName": "Groceries",
      "walletId": null,
      "walletName": null,
      "amount": 500.00,
      "spent": 320.50,
      "remaining": 179.50,
      "percentageUsed": 64.10,
      "currencyCode": "USD",
      "period": "MONTHLY",
      "startDate": "2025-10-01",
      "endDate": "2025-10-31",
      "status": "ACTIVE",
      "alertThreshold": 80.00,
      "alertEnabled": true,
      "alertSent": false,
      "rolloverEnabled": false,
      "createdAt": "2025-10-01T00:00:00"
    }
  ]
}
```

**Budget Periods:** `DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`, `CUSTOM`

**Budget Status:** `ACTIVE`, `PAUSED`, `COMPLETED`, `ARCHIVED`

### 6.2 Get Active Budgets

Get only active budgets.

**Endpoint:** `GET /api/budgets/active`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of active BudgetResponse objects
  ]
}
```

### 6.3 Get Current Period Budgets

Get budgets for current period.

**Endpoint:** `GET /api/budgets/current`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of current BudgetResponse objects
  ]
}
```

### 6.4 Get Budgets with Alerts

Get budgets exceeding alert threshold.

**Endpoint:** `GET /api/budgets/alerts`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of BudgetResponse objects with alerts
  ]
}
```

### 6.5 Get Exceeded Budgets

Get budgets that have been exceeded (100%+ spent).

**Endpoint:** `GET /api/budgets/exceeded`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    // Array of exceeded BudgetResponse objects
  ]
}
```

### 6.6 Get Budget by ID

Get specific budget details.

**Endpoint:** `GET /api/budgets/{budgetId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    // BudgetResponse object
  }
}
```

### 6.7 Create Budget

Create a new budget.

**Endpoint:** `POST /api/budgets`

**Request Body:**

```json
{
  "name": "Monthly Groceries",
  "description": "Food budget for the month",
  "amount": 500.00,
  "currencyCode": "USD",
  "period": "MONTHLY",
  "startDate": "2025-10-01",
  "endDate": "2025-10-31",
  "categoryId": "category-uuid",
  "walletId": null,
  "alertThreshold": 80.00,
  "alertEnabled": true,
  "rolloverEnabled": false
}
```

**Required Fields:** `name`, `amount`, `currencyCode`, `period`, `startDate`, `endDate`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Budget created successfully",
  "data": {
    // BudgetResponse object
  }
}
```

### 6.8 Update Budget

Update budget details.

**Endpoint:** `PUT /api/budgets/{budgetId}`

**Request Body:** Same as create

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Budget updated successfully",
  "data": {
    // Updated BudgetResponse object
  }
}
```

### 6.9 Pause Budget

Pause an active budget.

**Endpoint:** `POST /api/budgets/{budgetId}/pause`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Budget paused successfully"
}
```

### 6.10 Resume Budget

Resume a paused budget.

**Endpoint:** `POST /api/budgets/{budgetId}/resume`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Budget resumed successfully"
}
```

### 6.11 Delete Budget

Delete a budget.

**Endpoint:** `DELETE /api/budgets/{budgetId}`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Budget deleted successfully"
}
```

---

## 7. Reports & Analytics API

**Base Path:** `/api/reports`
**Authentication:** Required

### 7.1 Get Dashboard Summary

Get comprehensive dashboard summary with key metrics.

**Endpoint:** `GET /api/reports/dashboard?startDate=2025-10-01&endDate=2025-10-31`

**Query Parameters:**
- `startDate` (date, optional, default: first day of current month)
- `endDate` (date, optional, default: today)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "totalIncome": 5000.00,
    "totalExpenses": 3500.00,
    "netSavings": 1500.00,
    "totalBalance": 12000.00,
    "budgetUtilization": 70.00,
    "transactionCount": 85,
    "topSpendingCategory": "Groceries",
    "topSpendingAmount": 650.00
  }
}
```

### 7.2 Get Spending by Category

Get spending breakdown by category.

**Endpoint:** `GET /api/reports/spending-by-category?startDate=2025-10-01&endDate=2025-10-31`

**Query Parameters:**
- `startDate` (date, optional)
- `endDate` (date, optional)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "Groceries": 650.00,
    "Transportation": 320.00,
    "Entertainment": 250.00,
    "Utilities": 180.00,
    "Dining Out": 450.00
  }
}
```

### 7.3 Get Income by Category

Get income breakdown by category.

**Endpoint:** `GET /api/reports/income-by-category?startDate=2025-10-01&endDate=2025-10-31`

**Query Parameters:**
- `startDate` (date, optional)
- `endDate` (date, optional)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "Salary": 4500.00,
    "Freelance": 500.00,
    "Investments": 200.00
  }
}
```

### 7.4 Get Monthly Trends

Get monthly income/expense trends over time.

**Endpoint:** `GET /api/reports/trends?months=6`

**Query Parameters:**
- `months` (integer, optional, default: 6)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": {
    "labels": ["2025-05", "2025-06", "2025-07", "2025-08", "2025-09", "2025-10"],
    "income": [5000.00, 5200.00, 4800.00, 5500.00, 5000.00, 5300.00],
    "expenses": [3500.00, 3700.00, 3200.00, 4000.00, 3600.00, 3800.00],
    "net": [1500.00, 1500.00, 1600.00, 1500.00, 1400.00, 1500.00]
  }
}
```

### 7.5 Get Recent Transactions

Get recent transactions for dashboard display.

**Endpoint:** `GET /api/reports/recent-transactions?limit=10`

**Query Parameters:**
- `limit` (integer, optional, default: 10)

**Response:** `200 OK`

```json
{
  "success": true,
  "data": [
    {
      "id": "transaction-uuid",
      "amount": 45.99,
      "type": "EXPENSE",
      "category": "Groceries",
      "wallet": "Main Checking",
      "date": "2025-10-08",
      "description": "Grocery shopping"
    }
  ]
}
```

---

## Data Types & Formats

### Date/Time Formats

- **Date:** ISO 8601 format `YYYY-MM-DD` (e.g., `2025-10-08`)
- **DateTime:** ISO 8601 format `YYYY-MM-DDTHH:mm:ss` (e.g., `2025-10-08T10:30:00`)
- **UUID:** Standard UUID format (e.g., `123e4567-e89b-12d3-a456-426614174000`)

### Decimal Values

Monetary amounts are represented as decimal numbers with 2 decimal places:
- `1234.56`
- `0.99`
- `10000.00`

### Currency Codes

ISO 4217 3-letter currency codes (e.g., `USD`, `EUR`, `GBP`, `JPY`)

---

## Rate Limits

Currently, no rate limits are enforced. This may change in future versions.

---

## Support

For API issues or questions:
- GitHub Issues: https://github.com/yourusername/budget-tracker/issues
- API Documentation: http://localhost:8080/swagger-ui.html

---

**Built with ❤️ using Spring Boot 3.2.1 | API Version: 1.0**
