.PHONY: help build start stop restart logs clean test backend frontend infra

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Docker Commands
build: ## Build all Docker images
	docker-compose build

start: ## Start all services
	docker-compose up -d

stop: ## Stop all services
	docker-compose down

restart: ## Restart all services
	docker-compose restart

logs: ## Follow logs from all services
	docker-compose logs -f

logs-backend: ## Follow backend logs only
	docker-compose logs -f backend

# Infrastructure Commands
infra-up: ## Start infrastructure only (postgres, redis, kafka, minio)
	docker-compose up -d postgres redis kafka zookeeper minio prometheus grafana

infra-down: ## Stop infrastructure
	docker-compose stop postgres redis kafka zookeeper minio prometheus grafana

# Backend Commands
backend-build: ## Build backend with Maven
	cd backend && mvn clean install -DskipTests

backend-test: ## Run backend tests
	cd backend && mvn test

backend-run: ## Run backend locally
	cd backend && mvn spring-boot:run

backend-package: ## Package backend as JAR
	cd backend && mvn clean package

# Database Commands
db-migrate: ## Run database migrations
	cd backend && mvn flyway:migrate

db-clean: ## Clean database
	cd backend && mvn flyway:clean

db-info: ## Show migration info
	cd backend && mvn flyway:info

db-shell: ## Open PostgreSQL shell
	docker exec -it budget-tracker-postgres psql -U postgres -d budget_tracker

# Monitoring Commands
prometheus: ## Open Prometheus in browser
	open http://localhost:9090

grafana: ## Open Grafana in browser
	open http://localhost:3000

swagger: ## Open Swagger UI in browser
	open http://localhost:8080/swagger-ui.html

pgadmin: ## Open pgAdmin in browser
	open http://localhost:5050

# Cleanup Commands
clean: ## Stop and remove all containers, networks, volumes
	docker-compose down -v

clean-all: ## Remove all containers, networks, volumes, and images
	docker-compose down -v --rmi all

# Development Commands
dev: infra-up backend-run ## Start infra and run backend locally

setup: ## Initial project setup
	cp .env.example .env
	@echo "Please update .env file with your configuration"

# Testing Commands
test: ## Run all tests
	cd backend && mvn verify

test-unit: ## Run unit tests only
	cd backend && mvn test

test-integration: ## Run integration tests only
	cd backend && mvn verify -DskipUnitTests

coverage: ## Generate test coverage report
	cd backend && mvn test jacoco:report
	@echo "Coverage report: backend/target/site/jacoco/index.html"

# Health Check Commands
health: ## Check health of all services
	@echo "Backend:" && curl -s http://localhost:8080/actuator/health | jq .
	@echo "\nPostgreSQL:" && docker exec budget-tracker-postgres pg_isready
	@echo "\nRedis:" && docker exec budget-tracker-redis redis-cli ping
	@echo "\nKafka:" && docker exec budget-tracker-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Utility Commands
format: ## Format code
	cd backend && mvn spotless:apply

lint: ## Run linters
	cd backend && mvn checkstyle:check

# Production Commands
prod-build: ## Build for production
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

prod-up: ## Start production environment
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Backup Commands
backup-db: ## Backup database
	docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker > backup_$(shell date +%Y%m%d_%H%M%S).sql

restore-db: ## Restore database (usage: make restore-db FILE=backup.sql)
	docker exec -i budget-tracker-postgres psql -U postgres budget_tracker < $(FILE)
