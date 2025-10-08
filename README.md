# 💰 Budget Tracker

> A comprehensive SaaS personal finance and budget tracking application with multi-currency support, powerful analytics, and intelligent insights.

[![Version](https://img.shields.io/badge/version-v1.0-blue)]()
[![Backend](https://img.shields.io/badge/backend-complete-success)]()
[![Frontend](https://img.shields.io/badge/frontend-pending-orange)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

---

## ✨ Features

### Core Features
- 🔐 **Secure Authentication** - JWT-based auth with refresh tokens
- 💳 **Multi-Wallet Support** - Manage multiple accounts and wallets
- 💸 **Transaction Management** - Track income, expenses, and transfers
- 📊 **Budget Tracking** - Set and monitor budgets with alerts
- 🎯 **Savings Goals** - Track progress towards financial goals
- 🔄 **Recurring Transactions** - Automate regular income/expenses
- 🏷️ **Categories & Tags** - Organize transactions efficiently
- 💱 **Multi-Currency** - Support for 150+ currencies with real-time conversion

### Advanced Features
- 📈 **Analytics & Reports** - Comprehensive financial insights
- 🔔 **Budget Alerts** - Get notified before overspending
- 📱 **RESTful API** - Complete API for integrations
- 🔍 **Smart Search** - Find transactions instantly
- 📤 **Data Export** - Export to CSV, PDF, Excel
- 🌍 **Multi-Language** - Internationalization support
- 🎨 **Customizable** - Personalize categories, colors, icons

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- 4GB RAM minimum
- 10GB free disk space
- Java 17+ (for local development mode)
- Maven 3.8+ (for local development mode)

### 🎯 Easy Deployment (One Command)

```bash
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker
./deploy.sh
```

The interactive menu will guide you through:
1. **Local Development** - Infrastructure in Docker, backend with Maven (hot reload)
2. **Local Docker** - Full stack in Docker containers
3. **Production** - Production-optimized deployment

### 📋 Deployment Modes

#### 1. Local Development (Recommended for Development)
```bash
./deploy.sh
# Select option 1
```
- Infrastructure runs in Docker (PostgreSQL, Redis, Kafka, MinIO)
- Spring Boot runs locally with Maven for hot reload
- Best for: Active development

#### 2. Local Docker (Full Stack)
```bash
./deploy.sh
# Select option 2
```
- Everything runs in Docker containers
- Best for: Testing complete system, demos

#### 3. Production Deployment
```bash
./deploy.sh
# Select option 3
```
- Production-optimized configuration
- Resource limits and health checks
- Security hardening
- Best for: Production environments

### 🔧 Quick Commands

```bash
# Start development server
./deploy.sh  # Choose option 1

# View logs
./deploy.sh  # Choose option 5

# Health check all services
./deploy.sh  # Choose option 4

# Database backup
./deploy.sh  # Choose option 6

# Stop all services
docker-compose down

# Clean up everything
./deploy.sh  # Choose option 9
```

### 🌐 Access URLs

After deployment, access your services:

**Local Development:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health

**Infrastructure:**
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
- pgAdmin: http://localhost:5050 (admin@budgettracker.com/admin)
- Grafana: http://localhost:3001 (admin/admin)
- Prometheus: http://localhost:9090

**GitHub Codespaces:**
- Check the 'PORTS' tab in VS Code for forwarded URLs
- All ports are automatically forwarded

### 🧪 Quick API Test

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "demo@example.com",
    "password": "Demo123!",
    "firstName": "Demo",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "demo@example.com",
    "password": "Demo123!"
  }'
```

---

## 📚 Documentation

### 📖 Complete Documentation

Comprehensive documentation is available in the [`/docs`](docs/) folder:

| Document | Description |
|----------|-------------|
| [Quick Start Guide](docs/QUICKSTART.md) | Get up and running in 5 minutes |
| [API Reference](docs/API_REFERENCE.md) | Complete API documentation with all endpoints |
| [API Examples](docs/API_EXAMPLES.md) | Real-world usage examples and workflows |
| [Deployment Guide](docs/DEPLOYMENT.md) | Production deployment, configuration, and troubleshooting |

### 🔍 Interactive API Documentation

For live API testing and exploration, visit Swagger UI after starting the application:
```
http://localhost:8080/swagger-ui.html
```

---

## 🏗️ Architecture

### Technology Stack

**Backend**
- Spring Boot 3.2.1 (Java 17)
- PostgreSQL 16
- Redis 7
- Apache Kafka
- MinIO (S3-compatible)

**Frontend** (Planned)
- Next.js 14
- TypeScript
- Ant Design + ShadCN UI
- Zustand + React Query

**Infrastructure**
- Docker & Docker Compose
- Kubernetes
- Terraform (AWS)
- GitHub Actions (CI/CD)
- Prometheus + Grafana (Monitoring)

### System Overview

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Next.js   │─────▶│  Spring Boot │─────▶│ PostgreSQL  │
│  Frontend   │      │   Backend    │      │  Database   │
└─────────────┘      └──────────────┘      └─────────────┘
                            │
                            ├──────────▶ Redis (Cache)
                            ├──────────▶ Kafka (Events)
                            └──────────▶ MinIO (Storage)
```

---

## 📦 Project Structure

```
budget-tracker/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration & migrations
│   └── pom.xml             # Maven dependencies
├── infra/                   # Infrastructure as code
│   ├── grafana/            # Grafana dashboards
│   ├── kubernetes/         # K8s manifests
│   ├── prometheus/         # Prometheus config
│   └── terraform/          # AWS infrastructure
├── scripts/                 # Deployment and utility scripts
├── docs/                    # Documentation
│   └── DEPLOYMENT.md       # Deployment instructions
├── docker-compose.yml       # Local development
├── docker-compose.prod.yml  # Production configuration
├── deploy.sh               # One-command deployment
├── Makefile                # Build automation
└── README.md               # This file
```

---

## 🎯 Roadmap

### ✅ Phase 1: Backend API - v1.0 (Complete)
- [x] Core API development
- [x] Database schema & migrations
- [x] Authentication & authorization (JWT)
- [x] Wallet management
- [x] Transaction tracking
- [x] Budget management and alerts
- [x] Reports and analytics
- [x] Category management
- [x] Docker & Kubernetes setup
- [x] CI/CD pipeline
- [x] Monitoring & logging (Prometheus, Grafana)

### 🔄 Phase 2: Frontend (Planned)
- [ ] Next.js application setup
- [ ] Authentication UI
- [ ] Dashboard & analytics
- [ ] Transaction management UI
- [ ] Budget & goals UI
- [ ] Settings & profile pages

### 🔮 Phase 3: Advanced Features (Future)
- [ ] Savings goals tracking
- [ ] Recurring transactions
- [ ] Bank account integration (Plaid)
- [ ] OCR receipt scanning
- [ ] ML transaction categorization
- [ ] Investment tracking
- [ ] Mobile apps (React Native)
- [ ] Multi-tenant support

### 🚀 Phase 4: Production Launch
- [ ] Beta testing
- [ ] Performance optimization
- [ ] Security audit
- [ ] Production deployment
- [ ] User onboarding

---

## 🛠️ Development

### Local Development Setup

```bash
# Clone repository
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker

# Start development environment (interactive menu)
./deploy.sh

# Or directly with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f backend

# Access services
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# Grafana: http://localhost:3001
# pgAdmin: http://localhost:5050
```

---

## 📊 API Documentation

The API provides comprehensive endpoints for managing personal finances:

### Core Endpoints

- **Authentication** (`/api/auth`) - Register, login, token refresh
- **Wallets** (`/api/wallets`) - Multi-wallet management
- **Transactions** (`/api/transactions`) - Income, expenses, transfers
- **Budgets** (`/api/budgets`) - Budget creation and tracking
- **Categories** (`/api/categories`) - Custom categories
- **Reports** (`/api/reports`) - Analytics and insights

**Interactive API Documentation**: After starting the application, visit `http://localhost:8080/swagger-ui.html` for complete API documentation with live testing capabilities.

---

## 📝 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Spring Boot, PostgreSQL, and Docker**
