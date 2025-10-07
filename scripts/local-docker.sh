#!/bin/bash

###############################################################################
# Local Docker Mode
#
# Starts the full stack in Docker containers
# Best for: Testing the complete system locally
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_info() { echo -e "${BLUE}ℹ ${NC}$1"; }
print_success() { echo -e "${GREEN}✓ ${NC}$1"; }
print_error() { echo -e "${RED}✗ ${NC}$1"; }
print_warning() { echo -e "${YELLOW}⚠ ${NC}$1"; }

# Check if running in Codespaces
is_codespace() {
    [[ -n "${CODESPACES}" ]] || [[ -n "${CODESPACE_NAME}" ]]
}

# Build and start services
start_services() {
    print_info "Building and starting all services..."

    cd "$PROJECT_ROOT"

    # Check if backend Dockerfile exists
    if [ ! -f "backend/Dockerfile" ]; then
        print_error "Backend Dockerfile not found"
        exit 1
    fi

    # Build images
    print_info "Building Docker images (this may take a few minutes)..."
    docker-compose build --no-cache backend

    # Start all services
    print_info "Starting all services..."
    docker-compose up -d

    print_success "All services are starting..."
}

# Initialize database
initialize_database() {
    print_info "Initializing database..."

    # Wait for PostgreSQL to be ready first
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

# Wait for services
wait_for_services() {
    print_info "Waiting for services to be healthy (this may take 60-90 seconds)..."

    local max_wait=180
    local waited=0

    while [ $waited -lt $max_wait ]; do
        # Check backend health
        if docker exec budget-tracker-backend wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Backend is healthy!"
            return 0
        fi

        sleep 3
        waited=$((waited + 3))
        echo -n "."
    done

    print_warning "Services are taking longer than expected"
    print_info "Checking service status..."
    docker-compose ps
}

# Display URLs
display_urls() {
    echo ""
    print_success "════════════════════════════════════════════════"
    print_success "  All services are running!"
    print_success "════════════════════════════════════════════════"
    echo ""

    if is_codespace; then
        print_info "GitHub Codespaces URLs:"
        print_info "  Check the 'PORTS' tab in VS Code for forwarded URLs"
        echo ""
        print_info "Services (append to your codespace domain):"
        print_info "  Backend API: Port 8080"
        print_info "  Swagger UI: Port 8080/swagger-ui.html"
        print_info "  Grafana: Port 3000 (admin/admin)"
        print_info "  Prometheus: Port 9090"
        print_info "  MinIO Console: Port 9001 (minioadmin/minioadmin)"
        print_info "  pgAdmin: Port 5050 (admin@budgettracker.com/admin)"
    else
        print_info "Application URLs:"
        print_info "  Backend API: ${CYAN}http://localhost:8080${NC}"
        print_info "  Swagger UI: ${CYAN}http://localhost:8080/swagger-ui.html${NC}"
        print_info "  Actuator Health: ${CYAN}http://localhost:8080/actuator/health${NC}"
        echo ""
        print_info "Monitoring:"
        print_info "  Grafana: ${CYAN}http://localhost:3000${NC} (admin/admin)"
        print_info "  Prometheus: ${CYAN}http://localhost:9090${NC}"
        echo ""
        print_info "Infrastructure:"
        print_info "  MinIO Console: ${CYAN}http://localhost:9001${NC} (minioadmin/minioadmin)"
        print_info "  pgAdmin: ${CYAN}http://localhost:5050${NC} (admin@budgettracker.com/admin)"
        print_info "  PostgreSQL: localhost:5432"
        print_info "  Redis: localhost:6379"
        print_info "  Kafka: localhost:9092"
    fi

    echo ""
    print_success "════════════════════════════════════════════════"
    echo ""
}

# Show logs
show_logs() {
    print_info "Useful commands:"
    echo ""
    echo "  View all logs:        docker-compose logs -f"
    echo "  View backend logs:    docker-compose logs -f backend"
    echo "  Stop all services:    docker-compose down"
    echo "  Restart backend:      docker-compose restart backend"
    echo "  Check status:         docker-compose ps"
    echo ""
}

# Main execution
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}    Local Docker Mode (Full Stack)${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    start_services
    initialize_database
    wait_for_services
    display_urls
    show_logs

    print_info "View logs with: ${CYAN}docker-compose logs -f${NC}"
    print_info "Stop services with: ${CYAN}docker-compose down${NC}"
}

main
