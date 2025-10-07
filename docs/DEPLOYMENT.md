# 🚀 Budget Tracker - Deployment Guide

Complete guide for deploying Budget Tracker in various environments.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Deployment Modes](#deployment-modes)
- [Production Deployment](#production-deployment)
- [GitHub Codespaces](#github-codespaces)
- [Environment Configuration](#environment-configuration)
- [Database Management](#database-management)
- [Monitoring & Logs](#monitoring--logs)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)

---

## Overview

Budget Tracker provides a unified deployment tool (`deploy.sh`) that supports multiple deployment scenarios:

- **Local Development**: Infrastructure in Docker, Spring Boot with Maven (hot reload)
- **Local Docker**: Full stack in Docker containers
- **Production**: Production-optimized deployment with security hardening

---

## Prerequisites

### Required
- **Docker** 20.10+ and **Docker Compose** 2.0+
- **4GB RAM** minimum (8GB+ recommended for production)
- **10GB** free disk space

### Optional (for Local Development mode)
- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.8+**

### Installation Commands

**Ubuntu/Debian:**
```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt-get update
sudo apt-get install docker-compose-plugin

# Install Java 17
sudo apt-get install openjdk-17-jdk

# Install Maven
sudo apt-get install maven
```

**macOS:**
```bash
# Install Docker Desktop
# Download from: https://www.docker.com/products/docker-desktop

# Install Java 17
brew install openjdk@17

# Install Maven
brew install maven
```

---

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker
```

### 2. Run Deployment Tool

```bash
./deploy.sh
```

### 3. Select Deployment Mode

The interactive menu will guide you through the deployment options.

---

## Deployment Modes

### 1. Local Development Mode

**Best for:** Active development with hot reload

**What it does:**
- Starts infrastructure services in Docker (PostgreSQL, Redis, Kafka, MinIO)
- Runs Spring Boot locally using Maven
- Enables automatic code reload on changes

**Command:**
```bash
./deploy.sh
# Select option 1
```

**Features:**
- ✅ Hot reload with Spring Boot DevTools
- ✅ Fast iteration cycles
- ✅ Full debugging support
- ✅ Direct access to logs
- ✅ Low resource usage

**Accessing the Application:**
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MinIO Console: http://localhost:9001

**Stopping:**
```bash
# Stop backend: Ctrl+C
# Stop infrastructure: docker-compose down
```

---

### 2. Local Docker Mode (Full Stack)

**Best for:** Testing complete system, demos, integration testing

**What it does:**
- Runs everything in Docker containers
- Uses production-like setup locally
- Includes all monitoring tools

**Command:**
```bash
./deploy.sh
# Select option 2
```

**Features:**
- ✅ Complete isolated environment
- ✅ Production-like setup
- ✅ Easy cleanup
- ✅ No local Java/Maven needed
- ✅ Monitoring stack included

**Services Started:**
- Backend (Spring Boot)
- PostgreSQL database
- Redis cache
- Apache Kafka + Zookeeper
- MinIO (S3-compatible storage)
- Prometheus (metrics)
- Grafana (dashboards)
- pgAdmin (database management)

**Accessing Services:**
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- MinIO Console: http://localhost:9001
- pgAdmin: http://localhost:5050

**Viewing Logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Or use deployment tool
./deploy.sh  # Option 5
```

**Stopping:**
```bash
docker-compose down
```

---

### 3. Production Deployment

**Best for:** Production environments, staging servers

**What it does:**
- Deploys with production configuration
- Applies resource limits and security hardening
- Enables enhanced monitoring and health checks
- Uses `.env.production` for configuration

**Prerequisites:**
1. Create `.env.production` from template:
   ```bash
   cp .env.production.template .env.production
   ```

2. Edit `.env.production` and set all required values:
   - Database credentials
   - JWT secret (use strong random value!)
   - API keys (Stripe, FX rates, etc.)
   - Email configuration
   - S3/storage credentials

**Command:**
```bash
./deploy.sh
# Select option 3
```

**Production Features:**
- ✅ Resource limits (CPU/Memory)
- ✅ Auto-restart policies
- ✅ Health checks with proper timeouts
- ✅ Production JVM settings (optimized heap, GC)
- ✅ Log rotation and aggregation
- ✅ Security hardening
- ✅ Database backups
- ✅ Metrics and monitoring

**Important Settings:**

**JVM Configuration (Automatic):**
```bash
JAVA_OPTS=-Xms1g -Xmx3g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**Resource Limits:**
- Backend: 4GB RAM limit, 2GB reservation
- PostgreSQL: 2GB RAM limit
- Redis: 512MB RAM limit
- Kafka: 2GB RAM limit

---

## Production Deployment

### Step-by-Step Production Setup

#### 1. Server Preparation

```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt-get install docker-compose-plugin

# Install utilities
sudo apt-get install git curl wget
```

#### 2. Clone and Configure

```bash
# Clone repository
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker

# Create production environment file
cp .env.production.template .env.production

# Edit with production values
nano .env.production
```

#### 3. Generate Secure Secrets

```bash
# Generate JWT secret (256-bit minimum)
openssl rand -base64 64

# Generate Grafana secret
openssl rand -base64 32

# Update .env.production with generated values
```

#### 4. Deploy

```bash
./deploy.sh
# Select option 3 (Production Deployment)
```

#### 5. Verify Deployment

```bash
# Run health checks
./deploy.sh
# Select option 4

# Check all services are running
docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps

# Test API
curl http://localhost:8080/actuator/health
```

#### 6. Setup Reverse Proxy (Nginx)

**Install Nginx:**
```bash
sudo apt-get install nginx certbot python3-certbot-nginx
```

**Configure Nginx (`/etc/nginx/sites-available/budget-tracker`):**
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable and get SSL:**
```bash
sudo ln -s /etc/nginx/sites-available/budget-tracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
sudo certbot --nginx -d your-domain.com
```

#### 7. Setup Automated Backups

```bash
# Setup daily backups (runs at 2 AM)
./deploy.sh
# Select option 6, then option 3 (Setup automated backups)
```

#### 8. Configure Firewall

```bash
# Allow SSH, HTTP, HTTPS
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## GitHub Codespaces

Budget Tracker works seamlessly in GitHub Codespaces!

### Quick Start

1. **Open in Codespaces:**
   - Click "Code" → "Create codespace on main"

2. **Run Deployment:**
   ```bash
   ./deploy.sh
   # Select option 1 or 2
   ```

3. **Access Application:**
   - Check the "PORTS" tab in VS Code
   - All ports are automatically forwarded
   - URLs format: `https://<codespace-name>-<port>.app.github.dev`

### Codespace-Specific Features

- ✅ Automatic port forwarding
- ✅ Pre-configured environment
- ✅ No local setup required
- ✅ Works from any device
- ✅ Integrated with VS Code

**Example URLs:**
- API: `https://psychic-invention-xxxx-8080.app.github.dev`
- Swagger: `https://psychic-invention-xxxx-8080.app.github.dev/swagger-ui.html`

---

## Environment Configuration

### Environment Files

- **`.env.example`** - Template with all available options
- **`.env.local`** - Local development settings (created automatically)
- **`.env.production.template`** - Production template
- **`.env.production`** - Your production settings (NOT in git)

### Key Configuration Variables

**Database:**
```bash
DB_HOST=postgres
DB_PORT=5432
DB_NAME=budget_tracker
DB_USERNAME=postgres
DB_PASSWORD=<strong-password-here>
```

**JWT (CRITICAL for Security):**
```bash
# Generate with: openssl rand -base64 64
JWT_SECRET=<256-bit-random-secret>
JWT_EXPIRATION=86400000  # 24 hours
```

**CORS:**
```bash
CORS_ALLOWED_ORIGINS=https://your-frontend.com,https://app.your-domain.com
```

**Email:**
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=<your-sendgrid-api-key>
```

---

## Database Management

### Backup Database

```bash
# Interactive backup
./deploy.sh
# Select option 6

# Or directly
bash scripts/backup.sh
```

**Backups are stored in:** `./backups/`

**Automated Backups:**
```bash
# Setup cron job for daily backups at 2 AM
./deploy.sh
# Option 6 → Option 3
```

### Restore Database

```bash
# Interactive restore
./deploy.sh
# Select option 7

# Or directly
bash scripts/restore.sh
```

**⚠️ Warning:** Restore will drop the existing database!

### Manual Database Operations

```bash
# Connect to database
docker exec -it budget-tracker-postgres psql -U postgres -d budget_tracker

# Create manual backup
docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker > backup.sql

# Restore manual backup
cat backup.sql | docker exec -i budget-tracker-postgres psql -U postgres budget_tracker
```

---

## Monitoring & Logs

### Health Checks

```bash
# Run comprehensive health check
./deploy.sh
# Select option 4

# Or directly
bash scripts/health-check.sh
```

**Checks:**
- ✅ PostgreSQL connectivity
- ✅ Redis connection
- ✅ Kafka broker status
- ✅ Backend API health
- ✅ Resource usage
- ✅ Port availability

### Viewing Logs

```bash
# Interactive log viewer
./deploy.sh
# Select option 5

# Or directly
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Search logs
docker-compose logs | grep "ERROR"

# Export logs
bash scripts/logs.sh
# Select option 10
```

### Monitoring Stack

**Prometheus:**
- URL: http://localhost:9090
- Collects metrics from all services
- 30-day retention in production

**Grafana:**
- URL: http://localhost:3000
- Default credentials: admin/admin
- Pre-configured dashboards
- Visualizes Prometheus metrics

**Actuator Endpoints:**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

---

## Troubleshooting

### Common Issues

**Issue: Port 8080 already in use**

Solution:
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
./deploy.sh  # Local dev mode auto-detects and uses next available port
```

**Issue: Services not becoming healthy**

Solution:
```bash
# Check logs
docker-compose logs backend

# Verify all services are running
docker-compose ps

# Check resources
docker stats

# Restart services
docker-compose restart
```

**Issue: Database connection failed**

Solution:
```bash
# Ensure PostgreSQL is healthy
docker exec budget-tracker-postgres pg_isready -U postgres

# Check credentials in .env file
cat .env.local | grep DB_

# Restart database
docker-compose restart postgres
```

**Issue: Out of memory**

Solution:
```bash
# Check Docker memory allocation
docker system df

# Clean up unused containers/images
docker system prune -a

# For production, increase server RAM or adjust limits in docker-compose.prod.yml
```

### Debugging Tips

1. **Enable detailed logging:**
   ```bash
   # Edit application-dev.yml
   logging:
     level:
       com.budgettracker: DEBUG
   ```

2. **Check container logs:**
   ```bash
   docker-compose logs --tail=100 -f backend
   ```

3. **Inspect container:**
   ```bash
   docker exec -it budget-tracker-backend /bin/sh
   ```

4. **Verify network:**
   ```bash
   docker network inspect budget-tracker-network
   ```

---

## Security Best Practices

### Production Security Checklist

- [ ] Change all default passwords
- [ ] Generate strong JWT secret (256+ bits)
- [ ] Use strong database password
- [ ] Configure CORS properly
- [ ] Enable HTTPS/SSL with valid certificate
- [ ] Set up firewall rules
- [ ] Regular security updates
- [ ] Enable automated backups
- [ ] Monitor logs for suspicious activity
- [ ] Use secrets management (AWS Secrets Manager, Vault)
- [ ] Regular dependency updates
- [ ] Implement rate limiting
- [ ] Set up intrusion detection

### Recommended Security Measures

**1. Environment Variables:**
- Never commit `.env.production` to Git
- Use secret management tools
- Rotate secrets regularly

**2. Network Security:**
- Use private networks for internal communication
- Expose only necessary ports
- Implement firewall rules

**3. Database Security:**
- Use strong passwords
- Enable SSL connections
- Regular backups
- Limit network access

**4. Application Security:**
- Keep dependencies updated
- Regular security scanning
- Monitor for vulnerabilities
- Implement proper authentication

**5. SSL/TLS:**
```bash
# Use Let's Encrypt for free SSL
sudo certbot --nginx -d your-domain.com
```

**6. Monitoring:**
- Set up alerts for failed logins
- Monitor resource usage
- Track API usage patterns
- Review logs regularly

---

## Maintenance

### Regular Maintenance Tasks

**Daily:**
- Check health status
- Review error logs
- Monitor resource usage

**Weekly:**
- Review security logs
- Check backup status
- Update dependencies

**Monthly:**
- Security updates
- Database optimization
- Backup cleanup

### Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and deploy
./deploy.sh
# Select option 3 (Production)
```

### Cleanup

```bash
# Light cleanup (keep data)
./deploy.sh
# Select option 9, then option 1

# Full cleanup (remove all data)
./deploy.sh
# Select option 9, then option 2
```

---

## Support

For issues and questions:
- GitHub Issues: https://github.com/yourusername/budget-tracker/issues
- Documentation: https://github.com/yourusername/budget-tracker/docs

---

**Built with ❤️ | Deployment made easy with automated scripts**
