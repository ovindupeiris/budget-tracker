# Budget Tracker - Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development](#local-development)
3. [Docker Deployment](#docker-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [AWS Deployment with Terraform](#aws-deployment)
6. [Environment Variables](#environment-variables)
7. [Database Migrations](#database-migrations)
8. [Monitoring Setup](#monitoring-setup)
9. [Backup and Recovery](#backup-and-recovery)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- Docker 24+ and Docker Compose
- Kubernetes 1.28+ (for K8s deployment)
- kubectl (for K8s deployment)
- Terraform 1.0+ (for AWS deployment)
- AWS CLI (for AWS deployment)
- Java 17+ (for local development)
- Maven 3.9+ (for local development)

### Required Accounts
- Docker Hub account (for image registry)
- AWS account (for cloud deployment)
- Domain name (for production)
- SSL certificate (for production)

---

## Local Development

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/budget-tracker.git
cd budget-tracker
```

### 2. Set Up Environment
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Start Infrastructure
```bash
# Start databases and services
docker-compose up -d postgres redis kafka zookeeper minio
```

### 4. Run Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 5. Verify Installation
```bash
curl http://localhost:8080/actuator/health
```

---

## Docker Deployment

### Full Stack with Docker Compose

```bash
# Build and start all services
docker-compose up -d --build

# View logs
docker-compose logs -f backend

# Check service health
docker-compose ps

# Access services:
# - Backend: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000
# - pgAdmin: http://localhost:5050
```

### Production Docker Compose

```bash
# Use production configuration
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Scale backend
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale backend=3
```

---

## Kubernetes Deployment

### 1. Create Namespace
```bash
kubectl apply -f infra/kubernetes/namespace.yaml
```

### 2. Create Secrets
```bash
# Database password
kubectl create secret generic postgres-secret \
  --from-literal=POSTGRES_PASSWORD=your-secure-password \
  -n budget-tracker

# Backend secrets
kubectl create secret generic backend-secret \
  --from-literal=DB_PASSWORD=your-db-password \
  --from-literal=REDIS_PASSWORD=your-redis-password \
  --from-literal=JWT_SECRET=your-jwt-secret \
  -n budget-tracker
```

### 3. Deploy Database
```bash
kubectl apply -f infra/kubernetes/postgres-deployment.yaml
```

### 4. Deploy Redis
```bash
kubectl apply -f infra/kubernetes/redis-deployment.yaml
```

### 5. Build and Push Docker Image
```bash
# Build image
cd backend
docker build -t yourusername/budget-tracker-backend:latest .

# Push to registry
docker push yourusername/budget-tracker-backend:latest
```

### 6. Deploy Backend
```bash
kubectl apply -f infra/kubernetes/backend-deployment.yaml
```

### 7. Deploy Ingress
```bash
# Install nginx ingress controller first
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Deploy application ingress
kubectl apply -f infra/kubernetes/ingress.yaml
```

### 8. Verify Deployment
```bash
# Check pods
kubectl get pods -n budget-tracker

# Check services
kubectl get svc -n budget-tracker

# Check ingress
kubectl get ingress -n budget-tracker

# View logs
kubectl logs -f deployment/backend -n budget-tracker
```

---

## AWS Deployment with Terraform

### 1. Configure AWS CLI
```bash
aws configure
# Enter your AWS Access Key ID
# Enter your AWS Secret Access Key
# Enter default region: us-east-1
```

### 2. Initialize Terraform
```bash
cd infra/terraform/aws
terraform init
```

### 3. Create tfvars File
```bash
cat > terraform.tfvars << EOF
aws_region             = "us-east-1"
environment            = "production"
db_password            = "your-secure-db-password"
db_instance_class      = "db.t3.medium"
redis_node_type        = "cache.t3.medium"
eks_node_instance_type = "t3.large"
EOF
```

### 4. Plan Infrastructure
```bash
terraform plan
```

### 5. Apply Infrastructure
```bash
terraform apply
# Review changes and type 'yes' to proceed
```

### 6. Configure kubectl for EKS
```bash
aws eks update-kubeconfig --name budget-tracker-eks --region us-east-1
```

### 7. Deploy Application to EKS
```bash
# Follow Kubernetes deployment steps above
kubectl apply -f infra/kubernetes/
```

### 8. Configure DNS
```bash
# Get Load Balancer URL
kubectl get ingress -n budget-tracker

# Create CNAME record in Route53 or your DNS provider
# api.yourdomain.com -> ALB-URL
```

---

## Environment Variables

### Required Variables

#### Database
```bash
DB_HOST=postgres
DB_PORT=5432
DB_NAME=budget_tracker
DB_USERNAME=postgres
DB_PASSWORD=your-secure-password
```

#### Redis
```bash
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

#### JWT
```bash
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

#### S3/Storage
```bash
S3_ENDPOINT=https://s3.amazonaws.com
S3_REGION=us-east-1
S3_ACCESS_KEY=your-access-key
S3_SECRET_KEY=your-secret-key
S3_BUCKET_NAME=budget-tracker-attachments
```

### Optional Variables

#### Kafka
```bash
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

#### Features
```bash
FEATURE_BANK_INTEGRATION=true
FEATURE_OCR_RECEIPTS=true
FEATURE_ML_CATEGORIZATION=true
```

---

## Database Migrations

### Automatic Migrations (Recommended)
Flyway runs automatically on startup. No manual intervention needed.

### Manual Migrations
```bash
cd backend

# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Rollback (be careful!)
mvn flyway:clean
```

### Create New Migration
```bash
# Create file: backend/src/main/resources/db/migration/V1_0_X__description.sql
# Example: V1_0_3__add_payment_methods.sql

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Monitoring Setup

### Prometheus

Access: http://your-domain:9090

**Key Metrics to Monitor:**
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Request duration
- `jvm_memory_used_bytes` - Memory usage
- `hikaricp_connections_active` - Database connections
- `system_cpu_usage` - CPU usage

### Grafana

Access: http://your-domain:3000

**Setup:**
1. Login (admin/admin)
2. Add Prometheus datasource
3. Import dashboards:
   - Spring Boot 2.1 Statistics (ID: 10280)
   - JVM (Micrometer) (ID: 4701)
   - PostgreSQL Database (ID: 9628)

### Application Logs

```bash
# Docker
docker-compose logs -f backend

# Kubernetes
kubectl logs -f deployment/backend -n budget-tracker

# AWS CloudWatch
aws logs tail /aws/eks/budget-tracker/application --follow
```

---

## Backup and Recovery

### Database Backup

#### Docker
```bash
# Backup
docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore
docker exec -i budget-tracker-postgres psql -U postgres budget_tracker < backup.sql
```

#### Kubernetes
```bash
# Backup
kubectl exec -n budget-tracker postgres-0 -- pg_dump -U postgres budget_tracker > backup.sql

# Restore
kubectl exec -i -n budget-tracker postgres-0 -- psql -U postgres budget_tracker < backup.sql
```

#### AWS RDS
Automated backups enabled with 7-day retention.

```bash
# Manual snapshot
aws rds create-db-snapshot \
  --db-instance-identifier budget-tracker-db \
  --db-snapshot-identifier manual-backup-$(date +%Y%m%d)

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier budget-tracker-db-restored \
  --db-snapshot-identifier snapshot-name
```

### Application Data

#### S3 Backup
```bash
# Backup attachments
aws s3 sync s3://budget-tracker-attachments s3://budget-tracker-backups/$(date +%Y%m%d)/

# Restore
aws s3 sync s3://budget-tracker-backups/20250106/ s3://budget-tracker-attachments/
```

---

## Troubleshooting

### Backend Won't Start

```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Database not ready
docker-compose ps postgres
docker exec budget-tracker-postgres pg_isready

# 2. Port already in use
sudo lsof -i :8080
kill -9 <PID>

# 3. Out of memory
docker stats
# Increase memory in docker-compose.yml
```

### Database Connection Issues

```bash
# Test connection
docker exec budget-tracker-postgres psql -U postgres -d budget_tracker -c "SELECT 1"

# Check credentials
docker exec budget-tracker-postgres env | grep POSTGRES

# Reset password
docker exec -it budget-tracker-postgres psql -U postgres
ALTER USER postgres WITH PASSWORD 'newpassword';
```

### Kubernetes Pod Issues

```bash
# Check pod status
kubectl get pods -n budget-tracker

# Describe pod
kubectl describe pod <pod-name> -n budget-tracker

# Check logs
kubectl logs <pod-name> -n budget-tracker --previous

# Get shell access
kubectl exec -it <pod-name> -n budget-tracker -- /bin/sh
```

### Performance Issues

```bash
# Check resource usage
docker stats

# Kubernetes
kubectl top pods -n budget-tracker
kubectl top nodes

# Database performance
docker exec -it budget-tracker-postgres psql -U postgres -d budget_tracker
SELECT * FROM pg_stat_activity;
```

---

## Security Checklist

- [ ] Change all default passwords
- [ ] Use strong JWT secret (min 256 bits)
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Enable database encryption at rest
- [ ] Set up WAF (AWS WAF)
- [ ] Enable rate limiting
- [ ] Regular security updates
- [ ] Rotate secrets regularly
- [ ] Enable audit logging
- [ ] Configure backup encryption
- [ ] Use secrets manager (AWS Secrets Manager)

---

## Performance Tuning

### Database
```properties
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
```

### JVM
```yaml
# docker-compose.yml or K8s deployment
JAVA_OPTS: "-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Redis
```bash
# Increase memory
maxmemory 2gb
maxmemory-policy allkeys-lru
```

---

## Scaling

### Horizontal Scaling

#### Docker Compose
```bash
docker-compose up -d --scale backend=3
```

#### Kubernetes
```bash
kubectl scale deployment backend --replicas=5 -n budget-tracker
```

### Vertical Scaling

Update resource limits in deployment files and restart services.

---

## Support

For deployment issues:
- Check logs first
- Review this guide
- Check GitHub Issues
- Contact DevOps team

---

**Last Updated:** 2025-10-06
