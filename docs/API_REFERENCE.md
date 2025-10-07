# Budget Tracker API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All endpoints (except auth endpoints) require JWT authentication.

Include the token in the Authorization header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## Authentication Endpoints

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "currencyCode": "USD"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "status": "PENDING",
    "emailVerified": false,
    "currencyCode": "USD"
  }
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "emailOrUsername": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "username": "johndoe"
    }
  }
}
```

---

## User Endpoints

### Get Current User
```http
GET /api/users/me
Authorization: Bearer YOUR_TOKEN
```

### Update Profile
```http
PUT /api/users/me
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "timezone": "America/New_York",
  "locale": "en",
  "currencyCode": "USD"
}
```

### Change Password
```http
PUT /api/users/me/password?oldPassword=old&newPassword=new
Authorization: Bearer YOUR_TOKEN
```

---

## Wallet Endpoints

### Create Wallet
```http
POST /api/wallets
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "name": "My Checking Account",
  "description": "Primary checking account",
  "type": "CHECKING",
  "currencyCode": "USD",
  "initialBalance": 1000.00,
  "icon": "💰",
  "color": "#4CAF50",
  "isDefault": true
}
```

**Wallet Types:**
- CASH
- CHECKING
- SAVINGS
- CREDIT_CARD
- DEBIT_CARD
- INVESTMENT
- CRYPTO
- E_WALLET
- OTHER

### Get All Wallets
```http
GET /api/wallets
Authorization: Bearer YOUR_TOKEN
```

Optional query parameters:
- `includeArchived=true` - Include archived wallets

### Get Wallet by ID
```http
GET /api/wallets/{walletId}
Authorization: Bearer YOUR_TOKEN
```

### Update Wallet
```http
PUT /api/wallets/{walletId}
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated description"
}
```

### Delete Wallet
```http
DELETE /api/wallets/{walletId}
Authorization: Bearer YOUR_TOKEN
```

### Set Default Wallet
```http
PUT /api/wallets/{walletId}/set-default
Authorization: Bearer YOUR_TOKEN
```

### Archive/Unarchive Wallet
```http
PUT /api/wallets/{walletId}/archive
Authorization: Bearer YOUR_TOKEN

PUT /api/wallets/{walletId}/unarchive
Authorization: Bearer YOUR_TOKEN
```

### Get Total Balance
```http
GET /api/wallets/balance/total
Authorization: Bearer YOUR_TOKEN
```

Optional query parameters:
- `currencyCode=USD` - Filter by currency

---

## Transaction Endpoints

### Create Transaction
```http
POST /api/transactions
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "walletId": "wallet-uuid",
  "categoryId": "category-uuid",
  "type": "EXPENSE",
  "amount": 50.00,
  "currencyCode": "USD",
  "transactionDate": "2025-10-06",
  "description": "Grocery shopping",
  "merchantName": "Whole Foods",
  "location": "New York, NY"
}
```

**Transaction Types:**
- INCOME
- EXPENSE
- TRANSFER
- INVESTMENT
- FEE
- REFUND

### Get Transactions (Paginated)
```http
GET /api/transactions?page=0&size=20&sort=transactionDate,desc
Authorization: Bearer YOUR_TOKEN
```

### Get Transaction by ID
```http
GET /api/transactions/{transactionId}
Authorization: Bearer YOUR_TOKEN
```

### Get Transactions by Date Range
```http
GET /api/transactions/date-range?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer YOUR_TOKEN
```

### Search Transactions
```http
GET /api/transactions/search?query=grocery
Authorization: Bearer YOUR_TOKEN
```

### Update Transaction
```http
PUT /api/transactions/{transactionId}
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "description": "Updated description",
  "amount": 55.00
}
```

### Delete Transaction
```http
DELETE /api/transactions/{transactionId}
Authorization: Bearer YOUR_TOKEN
```

### Reconcile Transaction
```http
PUT /api/transactions/{transactionId}/reconcile
Authorization: Bearer YOUR_TOKEN
```

### Get Income Statistics
```http
GET /api/transactions/stats/income?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer YOUR_TOKEN
```

### Get Expense Statistics
```http
GET /api/transactions/stats/expenses?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer YOUR_TOKEN
```

---

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-10-06T12:00:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message",
    "details": { ... }
  },
  "timestamp": "2025-10-06T12:00:00"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

---

## Error Codes

- `RESOURCE_NOT_FOUND` - Requested resource not found
- `UNAUTHORIZED` - Authentication required or invalid
- `ACCESS_DENIED` - Insufficient permissions
- `VALIDATION_ERROR` - Input validation failed
- `BUSINESS_ERROR` - Business rule violation
- `INTERNAL_SERVER_ERROR` - Unexpected server error

---

## Rate Limiting

- Default: 100 requests per minute per user
- Authenticated users: Higher limits based on subscription tier
- Exceeded limit response: HTTP 429 Too Many Requests

---

## Pagination

All list endpoints support pagination with these parameters:

- `page` - Page number (0-based, default: 0)
- `size` - Page size (default: 20, max: 100)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

---

## Date Formats

- Dates: ISO 8601 format (`YYYY-MM-DD`)
- DateTimes: ISO 8601 format (`YYYY-MM-DDTHH:mm:ss`)
- Timezone: UTC by default

---

## Currency Codes

Use ISO 4217 currency codes (e.g., USD, EUR, GBP, JPY)

---

## Swagger/OpenAPI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8080/api-docs
```

---

## Webhooks (Coming Soon)

Subscribe to events:
- transaction.created
- transaction.updated
- budget.alert
- goal.completed
- subscription.due

---

## SDKs and Client Libraries (Coming Soon)

- JavaScript/TypeScript
- Python
- Java
- PHP
- Ruby
