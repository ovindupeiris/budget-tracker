#!/bin/bash

###############################################################################
# Local Development Mode
#
# Starts infrastructure in Docker and runs Spring Boot locally with Maven
# Best for: Active development with hot reload
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

# Get available port
get_available_port() {
    local start_port=${1:-8080}
    local port=$start_port

    while lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; do
        print_warning "Port $port is in use, trying next port..."
        port=$((port + 1))
    done

    echo $port
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        print_info "Installing Java 17..."
        sudo apt-get update && sudo apt-get install -y openjdk-17-jdk
    fi

    # Check Java version
    java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_warning "Java version is less than 17"
        print_info "Setting JAVA_HOME to Java 17..."
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
        export PATH=$JAVA_HOME/bin:$PATH
    fi

    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi

    print_success "Prerequisites check passed"
}

# Start infrastructure
start_infrastructure() {
    print_info "Starting infrastructure services..."

    cd "$PROJECT_ROOT"

    # Start only infrastructure services
    docker-compose up -d postgres redis kafka zookeeper minio

    print_success "Infrastructure services starting..."

    # Wait for services to be healthy
    print_info "Waiting for services to be healthy (this may take 30-60 seconds)..."

    local max_wait=120
    local waited=0

    while [ $waited -lt $max_wait ]; do
        local healthy=true

        # Check Postgres
        if ! docker exec budget-tracker-postgres pg_isready -U postgres > /dev/null 2>&1; then
            healthy=false
        fi

        # Check Redis
        if ! docker exec budget-tracker-redis redis-cli -a redis_password ping > /dev/null 2>&1; then
            healthy=false
        fi

        if [ "$healthy" = true ]; then
            print_success "All infrastructure services are healthy!"
            break
        fi

        sleep 2
        waited=$((waited + 2))
        echo -n "."
    done

    if [ $waited -ge $max_wait ]; then
        print_error "Services did not become healthy in time"
        exit 1
    fi

    echo ""
}

# Initialize database
initialize_database() {
    print_info "Checking database setup..."

    # Source the database initialization script
    if [ -f "$SCRIPT_DIR/init-database.sh" ]; then
        bash "$SCRIPT_DIR/init-database.sh"
        if [ $? -ne 0 ]; then
            print_error "Database initialization failed"
            exit 1
        fi
    else
        print_warning "Database initialization script not found, skipping..."
    fi
}

# Start backend
start_backend() {
    print_info "Starting Spring Boot backend..."

    cd "$PROJECT_ROOT/backend"

    # Find available port
    local port=$(get_available_port 8080)

    if [ $port -ne 8080 ]; then
        print_warning "Port 8080 is in use, using port $port instead"
    fi

    # Set JAVA_HOME
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH

    print_info "Backend will start on port: $port"

    if is_codespace; then
        print_success "GitHub Codespaces detected!"
        print_info "Once started, your application will be available at:"
        print_info "  ${CYAN}https://\${CODESPACE_NAME}-${port}.app.github.dev${NC}"
        print_info "  Swagger UI: ${CYAN}https://\${CODESPACE_NAME}-${port}.app.github.dev/swagger-ui.html${NC}"
        echo ""
        print_info "Check the 'PORTS' tab in VS Code to get the exact URL"
    else
        print_success "Application will be available at:"
        print_info "  API: http://localhost:$port"
        print_info "  Swagger UI: http://localhost:$port/swagger-ui.html"
        print_info "  Actuator: http://localhost:$port/actuator/health"
    fi

    echo ""
    print_info "Starting Maven (this may take a minute on first run)..."
    echo ""

    # Run Spring Boot with custom port
    if [ $port -eq 8080 ]; then
        mvn spring-boot:run
    else
        mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$port"
    fi
}

# Display URLs
display_urls() {
    echo ""
    print_success "Infrastructure services running:"
    print_info "  PostgreSQL: localhost:5432"
    print_info "  Redis: localhost:6379"
    print_info "  Kafka: localhost:9092"
    print_info "  MinIO: http://localhost:9000 (admin/minioadmin)"
    print_info "  MinIO Console: http://localhost:9001"
    echo ""
}

# Main execution
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}    Local Development Mode${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    check_prerequisites
    start_infrastructure
    initialize_database
    display_urls

    echo ""
    print_info "Press Ctrl+C to stop the backend (infrastructure will keep running)"
    echo ""

    start_backend
}

# Cleanup on exit
cleanup() {
    echo ""
    print_warning "Shutting down..."
    print_info "Infrastructure services are still running"
    print_info "To stop them, run: docker-compose down"
}

trap cleanup EXIT

main
