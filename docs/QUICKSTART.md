# 🚀 Quick Start Guide

Get Budget Tracker running in 5 minutes!

## Prerequisites

- Ubuntu 20.04+ (or similar Linux)
- 4GB RAM minimum
- 20GB free disk space
- sudo access

## One-Command Deployment

```bash
cd /home/ovindu-peiris/Projects/budget-tracker
sudo bash deploy.sh
```

That's it! The script will:
- ✅ Install Docker & Docker Compose
- ✅ Build the application
- ✅ Start all services
- ✅ Verify everything is working

## Verify Installation

```bash
# Check health
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

## Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Backend API** | http://localhost:8080 | - |
| **Swagger Docs** | http://localhost:8080/swagger-ui.html | - |
| **Grafana** | http://localhost:3001 | admin / admin |
| **pgAdmin** | http://localhost:5050 | admin@admin.com / admin |
| **Prometheus** | http://localhost:9090 | - |
| **MinIO** | http://localhost:9001 | minioadmin / minioadmin |

## Quick API Test

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Save the `accessToken` from the response!**

### 3. Create a Wallet

```bash
TOKEN="your_access_token_here"

curl -X POST http://localhost:8080/api/wallets \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "My Wallet",
    "type": "BANK_ACCOUNT",
    "currency": "USD",
    "initialBalance": 1000.00
  }'
```

### 4. Create a Transaction

```bash
WALLET_ID="wallet_id_from_above"

curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "walletId": "'$WALLET_ID'",
    "type": "EXPENSE",
    "amount": 45.50,
    "currency": "USD",
    "description": "Grocery shopping",
    "transactionDate": "2025-10-07"
  }'
```

## Common Commands

```bash
# View logs
sudo docker-compose logs -f backend

# Stop all services
sudo docker-compose down

# Restart backend
sudo docker-compose restart backend

# View running containers
sudo docker-compose ps
```

## Next Steps

- 📖 Read the [API Reference](API_REFERENCE.md) for complete API documentation
- 🏗️ Check [Architecture Overview](ARCHITECTURE.md) to understand the system
- 🛠️ See [Development Guide](DEVELOPMENT.md) to start coding
- 📊 Explore [Monitoring Guide](MONITORING.md) for metrics and dashboards

## Troubleshooting

**Port already in use?**
```bash
# Find what's using port 8080
lsof -i :8080
# Kill it or change port in docker-compose.yml
```

**Backend not starting?**
```bash
# Check logs
sudo docker-compose logs backend

# Rebuild
sudo docker-compose build --no-cache backend
sudo docker-compose up -d backend
```

**Need help?** Check [Troubleshooting Guide](TROUBLESHOOTING.md)

---

✨ **You're all set!** Visit http://localhost:8080/swagger-ui.html to explore the API.
