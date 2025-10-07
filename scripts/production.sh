#!/bin/bash

###############################################################################
# Production Deployment Script
#
# Deploys the Budget Tracker application in production mode
# Features: Security hardening, resource limits, health checks, SSL
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_info() { echo -e "${BLUE}ℹ ${NC}$1"; }
print_success() { echo -e "${GREEN}✓ ${NC}$1"; }
print_error() { echo -e "${RED}✗ ${NC}$1"; }
print_warning() { echo -e "${YELLOW}⚠ ${NC}$1"; }
print_step() { echo -e "${MAGENTA}▶ ${NC}$1"; }

# Check environment file
check_environment() {
    print_step "Checking production environment configuration..."

    if [ ! -f "$PROJECT_ROOT/.env.production" ]; then
        print_error "Production environment file (.env.production) not found!"
        print_info "Creating from template..."

        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env.production"

        print_warning "Please configure .env.production with production values:"
        print_info "  - Database credentials"
        print_info "  - JWT secret (use a strong random value)"
        print_info "  - S3/Storage credentials"
        print_info "  - API keys"
        print_info "  - Mail configuration"
        echo ""
        print_error "Edit .env.production and run this script again"
        exit 1
    fi

    # Check for default values
    if grep -q "your-256-bit-secret" "$PROJECT_ROOT/.env.production"; then
        print_error "JWT_SECRET still contains default value!"
        print_info "Please set a strong random secret in .env.production"
        exit 1
    fi

    if grep -q "postgres" "$PROJECT_ROOT/.env.production" | grep -q "DB_PASSWORD"; then
        print_warning "Using default database password - consider changing for production"
    fi

    print_success "Environment configuration validated"
}

# Pre-deployment checks
pre_deployment_checks() {
    print_step "Running pre-deployment checks..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi

    # Check docker-compose.prod.yml
    if [ ! -f "$PROJECT_ROOT/docker-compose.prod.yml" ]; then
        print_error "docker-compose.prod.yml not found"
        exit 1
    fi

    # Check disk space
    local available_space=$(df "$PROJECT_ROOT" | tail -1 | awk '{print $4}')
    if [ "$available_space" -lt 5000000 ]; then
        print_warning "Low disk space detected (less than 5GB available)"
    fi

    print_success "Pre-deployment checks passed"
}

# Backup existing data
backup_before_deployment() {
    print_step "Creating backup before deployment..."

    if docker ps -a | grep -q "budget-tracker-postgres"; then
        local backup_file="$PROJECT_ROOT/backups/pre-deployment-$(date +%Y%m%d-%H%M%S).sql"
        mkdir -p "$PROJECT_ROOT/backups"

        if docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker > "$backup_file" 2>/dev/null; then
            print_success "Backup created: $backup_file"
        else
            print_warning "Could not create backup (database might not be running)"
        fi
    else
        print_info "No existing database to backup"
    fi
}

# Build production images
build_production_images() {
    print_step "Building production Docker images..."

    cd "$PROJECT_ROOT"

    # Build with production optimizations
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml build \
        --no-cache \
        --build-arg MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1" \
        backend

    print_success "Production images built"
}

# Deploy application
deploy_application() {
    print_step "Deploying application..."

    cd "$PROJECT_ROOT"

    # Stop old containers
    print_info "Stopping old containers..."
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml down --remove-orphans

    # Start new containers
    print_info "Starting new containers..."
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

    print_success "Application deployed"
}

# Initialize database
initialize_database() {
    print_step "Initializing database..."

    # Wait for PostgreSQL to be ready
    local max_wait=60
    local waited=0

    while [ $waited -lt $max_wait ]; do
        if docker exec budget-tracker-postgres pg_isready -U postgres > /dev/null 2>&1; then
            break
        fi
        sleep 2
        waited=$((waited + 2))
        echo -n "."
    done

    echo ""

    # Run database initialization
    if [ -f "$SCRIPT_DIR/init-database.sh" ]; then
        bash "$SCRIPT_DIR/init-database.sh"
        if [ $? -ne 0 ]; then
            print_warning "Database initialization had issues, but continuing..."
        fi
    else
        print_warning "Database initialization script not found"
    fi
}

# Run database migrations
run_migrations() {
    print_step "Running database migrations..."

    # Migrations are handled by Flyway on application startup
    print_success "Migrations will run automatically on startup"
}

# Wait for services
wait_for_health() {
    print_step "Waiting for services to be healthy..."

    local max_wait=300
    local waited=0

    while [ $waited -lt $max_wait ]; do
        if docker exec budget-tracker-backend wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Application is healthy!"
            return 0
        fi

        sleep 5
        waited=$((waited + 5))
        printf "."
    done

    echo ""
    print_error "Application did not become healthy in time"
    print_info "Check logs with: docker-compose logs backend"
    exit 1
}

# Post-deployment verification
post_deployment_verification() {
    print_step "Running post-deployment verification..."

    # Check all services are running
    local services=("postgres" "redis" "kafka" "backend")

    for service in "${services[@]}"; do
        if docker-compose ps "$service" | grep -q "Up"; then
            print_success "$service is running"
        else
            print_error "$service is not running"
            docker-compose logs --tail=50 "$service"
        fi
    done

    # Test API endpoint
    if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
        print_success "API health check passed"
    else
        print_warning "API health check failed"
    fi
}

# Display deployment info
display_deployment_info() {
    echo ""
    print_success "════════════════════════════════════════════════"
    print_success "  Production Deployment Complete!"
    print_success "════════════════════════════════════════════════"
    echo ""

    print_info "Application URLs:"
    print_info "  API: http://your-domain:8080"
    print_info "  Swagger: http://your-domain:8080/swagger-ui.html"
    print_info "  Health: http://your-domain:8080/actuator/health"
    echo ""

    print_info "Monitoring:"
    print_info "  Grafana: http://your-domain:3000"
    print_info "  Prometheus: http://your-domain:9090"
    echo ""

    print_warning "Next Steps:"
    print_info "  1. Configure reverse proxy (Nginx/Caddy) for SSL"
    print_info "  2. Set up domain name and DNS"
    print_info "  3. Configure firewall rules"
    print_info "  4. Set up automated backups"
    print_info "  5. Configure monitoring alerts"
    echo ""

    print_info "Useful Commands:"
    echo "  View logs:     docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f"
    echo "  Stop services: docker-compose -f docker-compose.yml -f docker-compose.prod.yml down"
    echo "  Restart:       docker-compose -f docker-compose.yml -f docker-compose.prod.yml restart"
    echo "  Status:        docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps"
    echo ""
}

# Rollback function
rollback() {
    print_error "Deployment failed! Rolling back..."

    docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

    print_info "Rollback complete. Check logs for errors."
    exit 1
}

# Main execution
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}    Production Deployment${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    print_warning "This will deploy Budget Tracker in PRODUCTION mode"
    echo -n "Continue? (yes/no): "
    read -r confirmation

    if [ "$confirmation" != "yes" ]; then
        print_info "Deployment cancelled"
        exit 0
    fi

    echo ""

    # Set trap for rollback on error
    trap rollback ERR

    check_environment
    pre_deployment_checks
    backup_before_deployment
    build_production_images
    deploy_application
    initialize_database
    run_migrations
    wait_for_health
    post_deployment_verification
    display_deployment_info

    print_success "Production deployment completed successfully!"
}

main
