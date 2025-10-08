# ⚡ Budget Tracker - Quick Start Guide

Get started with Budget Tracker in 5 minutes!

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [First Run](#first-run)
- [Your First API Calls](#your-first-api-calls)
- [Next Steps](#next-steps)

---

## Prerequisites

Before you begin, ensure you have:

- **Docker** and **Docker Compose** installed
- **4GB RAM** minimum (8GB recommended)
- **10GB** free disk space
- **Port 8080** available (or the deployment script will find an alternative)

### Check Docker Installation

```bash
docker --version
docker-compose --version
```

If Docker is not installed, visit: https://docs.docker.com/get-docker/

---

## Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker
```

### Step 2: Start the Application

**Option A: Interactive Deployment (Recommended)**

```bash
./deploy.sh
```

Select **Option 2** (Local Docker - Full Stack) for your first run.

**Option B: Direct Docker Compose**

```bash
docker-compose up -d
```

### Step 3: Wait for Services to Start

The deployment script will show you the progress. Wait for all services to become healthy (typically 30-60 seconds).

You can check the status:

```bash
docker-compose ps
```

All services should show status as "healthy" or "running".

---

## First Run

### Access the Application

Once all services are running:

**API Endpoints:**
- Backend API: http://localhost:8080
- API Documentation (Swagger): http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

**Infrastructure Tools:**
- Grafana (Monitoring): http://localhost:3001 (admin/admin)
- Prometheus (Metrics): http://localhost:9090
- pgAdmin (Database): http://localhost:5050 (admin@budgettracker.com/admin)
- MinIO Console (Storage): http://localhost:9001 (minioadmin/minioadmin)

### Verify Installation

Open your browser and visit:

```
http://localhost:8080/swagger-ui.html
```

You should see the interactive API documentation.

---

## Your First API Calls

### 1. Register a New User

Open your terminal and run:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "demo@example.com",
    "password": "Demo123!",
    "firstName": "Demo",
    "lastName": "User",
    "currencyCode": "USD"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "user-id",
    "email": "demo@example.com",
    "firstName": "Demo",
    "lastName": "User",
    "currencyCode": "USD"
  }
}
```

### 2. Login and Get Access Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "emailOrUsername": "demo@example.com",
    "password": "Demo123!"
  }'
```

**Save the access token** from the response:

```bash
export TOKEN="paste_your_access_token_here"
```

### 3. Create Your First Wallet

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Main Checking",
    "type": "CHECKING",
    "currencyCode": "USD",
    "initialBalance": 1000.00,
    "isDefault": true
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "wallet-id",
    "name": "Main Checking",
    "type": "CHECKING",
    "balance": 1000.00,
    "currencyCode": "USD",
    "isDefault": true
  }
}
```

### 4. Create a Category

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Groceries",
    "type": "EXPENSE",
    "icon": "shopping-cart",
    "color": "#FF5722"
  }'
```

**Save the category ID** from the response.

### 5. Record Your First Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "your-wallet-id",
    "categoryId": "your-category-id",
    "type": "EXPENSE",
    "amount": 50.00,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Grocery shopping"
  }'
```

### 6. Create a Budget

```bash
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Monthly Groceries",
    "amount": 500.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "your-category-id",
    "alertThreshold": 80.00,
    "alertEnabled": true
  }'
```

### 7. Get Your Dashboard Summary

```bash
curl -X GET "http://localhost:8080/api/reports/dashboard" \
  -H "Authorization: Bearer $TOKEN"
```

**You should see:**

```json
{
  "success": true,
  "data": {
    "totalIncome": 0.00,
    "totalExpenses": 50.00,
    "netSavings": -50.00,
    "totalBalance": 950.00,
    "transactionCount": 1
  }
}
```

---

## Exploring with Swagger UI

The easiest way to explore the API is using Swagger UI:

1. Open http://localhost:8080/swagger-ui.html
2. Click **"Authorize"** button at the top
3. Enter: `Bearer your_access_token_here`
4. Click **"Authorize"**
5. Now you can try out any endpoint directly in the browser!

### Benefits of Swagger UI:

- ✅ See all available endpoints
- ✅ View request/response schemas
- ✅ Test APIs interactively
- ✅ No need for curl or Postman
- ✅ Automatic documentation

---

## Quick Workflow Example

Here's a complete workflow to track your monthly expenses:

```bash
# 1. Register and login (save token)
# 2. Create wallets
curl -X POST http://localhost:8080/api/wallets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Checking Account",
    "type": "CHECKING",
    "currencyCode": "USD",
    "initialBalance": 3000.00,
    "isDefault": true
  }'

# 3. Create categories
categories=("Groceries" "Transportation" "Entertainment" "Utilities")
for cat in "${categories[@]}"; do
  curl -X POST http://localhost:8080/api/categories \
    -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' \
    -d "{
      \"name\": \"$cat\",
      \"type\": \"EXPENSE\"
    }"
done

# 4. Create budgets for each category
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Monthly Groceries",
    "amount": 600.00,
    "currencyCode": "USD",
    "period": "MONTHLY",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "categoryId": "groceries-category-id",
    "alertThreshold": 80.00
  }'

# 5. Start tracking transactions
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "walletId": "wallet-id",
    "categoryId": "groceries-category-id",
    "type": "EXPENSE",
    "amount": 85.50,
    "currencyCode": "USD",
    "transactionDate": "2025-10-08",
    "description": "Weekly grocery shopping"
  }'

# 6. Check your progress
curl -X GET "http://localhost:8080/api/reports/dashboard" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Useful Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Or use deployment tool
./deploy.sh  # Select option 5
```

### Stop Services

```bash
docker-compose down
```

### Restart Services

```bash
docker-compose restart
```

### Check Service Health

```bash
./deploy.sh  # Select option 4
```

### Backup Database

```bash
./deploy.sh  # Select option 6
```

---

## Troubleshooting

### Problem: Port 8080 is already in use

**Solution:** The deployment script will automatically detect and use an alternative port. Or you can manually stop the service using port 8080:

```bash
lsof -ti :8080 | xargs kill -9
```

### Problem: Services won't start

**Solution:**

```bash
# Check Docker status
docker-compose ps

# View logs for errors
docker-compose logs backend

# Restart services
docker-compose restart
```

### Problem: "Connection refused" errors

**Solution:** Wait for all services to fully start (30-60 seconds). Check health:

```bash
curl http://localhost:8080/actuator/health
```

Should return: `{"status":"UP"}`

### Problem: Database connection errors

**Solution:**

```bash
# Check PostgreSQL status
docker exec budget-tracker-postgres pg_isready -U postgres

# Restart database
docker-compose restart postgres
```

---

## Next Steps

Now that you're up and running, explore these resources:

### 📖 Documentation

- **[API Reference](API_REFERENCE.md)** - Complete API documentation for all endpoints
- **[API Examples](API_EXAMPLES.md)** - Real-world usage examples and workflows
- **[Deployment Guide](DEPLOYMENT.md)** - Production deployment and configuration

### 🎯 What to Do Next

1. **Explore the API** - Use Swagger UI to test all endpoints
2. **Set up your financial structure** - Create wallets, categories, and budgets
3. **Start tracking** - Record your transactions regularly
4. **Review reports** - Use the analytics endpoints to gain insights
5. **Integrate** - Build a frontend or mobile app using the API

### 💡 Tips for Success

- **Be consistent** - Record transactions daily or weekly
- **Use categories** - Organize transactions for better insights
- **Set realistic budgets** - Track spending patterns first, then set budgets
- **Enable alerts** - Get notified before overspending
- **Review regularly** - Check reports monthly to adjust spending habits

---

## Common API Workflows

### Daily Expense Tracking

```bash
# Record expense
POST /api/transactions

# Check remaining budget
GET /api/budgets/active
```

### Monthly Review

```bash
# Get dashboard summary
GET /api/reports/dashboard

# Get spending by category
GET /api/reports/spending-by-category

# Get monthly trends
GET /api/reports/trends
```

### Budget Management

```bash
# Create monthly budgets
POST /api/budgets

# Check budget status
GET /api/budgets/active

# Get alerts
GET /api/budgets/alerts
```

---

## Getting Help

If you run into issues:

1. **Check the logs:** `docker-compose logs -f backend`
2. **Review documentation:** [API Reference](API_REFERENCE.md)
3. **Use Swagger UI:** http://localhost:8080/swagger-ui.html
4. **Open an issue:** https://github.com/yourusername/budget-tracker/issues

---

## Summary

Congratulations! 🎉 You now have:

- ✅ Budget Tracker running locally
- ✅ Created a user account
- ✅ Set up your first wallet
- ✅ Recorded a transaction
- ✅ Created a budget
- ✅ Viewed your dashboard

You're ready to start tracking your finances! For more advanced features and detailed documentation, check out the [API Reference](API_REFERENCE.md) and [API Examples](API_EXAMPLES.md).

---

**Built with ❤️ using Spring Boot | Happy Budget Tracking! 💰**
