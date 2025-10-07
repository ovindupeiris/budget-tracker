#!/bin/bash

###############################################################################
# Health Check Script
#
# Comprehensive health checking for all services
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

# Check if container is running
check_container() {
    local container_name=$1
    if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
        return 0
    else
        return 1
    fi
}

# Check PostgreSQL
check_postgres() {
    echo -n "PostgreSQL:        "
    if check_container "budget-tracker-postgres"; then
        if docker exec budget-tracker-postgres pg_isready -U postgres > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY (not ready)"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check Redis
check_redis() {
    echo -n "Redis:             "
    if check_container "budget-tracker-redis"; then
        if docker exec budget-tracker-redis redis-cli -a redis_password ping > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY (not responding)"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check Kafka
check_kafka() {
    echo -n "Kafka:             "
    if check_container "budget-tracker-kafka"; then
        if docker exec budget-tracker-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY (broker not ready)"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check Zookeeper
check_zookeeper() {
    echo -n "Zookeeper:         "
    if check_container "budget-tracker-zookeeper"; then
        if docker exec budget-tracker-zookeeper echo ruok | nc localhost 2181 | grep -q imok 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check MinIO
check_minio() {
    echo -n "MinIO:             "
    if check_container "budget-tracker-minio"; then
        if curl -f -s http://localhost:9000/minio/health/live > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check Backend
check_backend() {
    echo -n "Backend:           "
    if check_container "budget-tracker-backend"; then
        if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            local health_status=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            if [ "$health_status" = "UP" ]; then
                print_success "HEALTHY (UP)"
                return 0
            else
                print_warning "DEGRADED ($health_status)"
                return 1
            fi
        else
            print_error "UNHEALTHY (not responding)"
            return 1
        fi
    else
        print_error "NOT RUNNING"
        return 1
    fi
}

# Check Prometheus
check_prometheus() {
    echo -n "Prometheus:        "
    if check_container "budget-tracker-prometheus"; then
        if curl -f -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY"
            return 1
        fi
    else
        print_warning "NOT RUNNING (optional)"
        return 0
    fi
}

# Check Grafana
check_grafana() {
    echo -n "Grafana:           "
    if check_container "budget-tracker-grafana"; then
        if curl -f -s http://localhost:3000/api/health > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY"
            return 1
        fi
    else
        print_warning "NOT RUNNING (optional)"
        return 0
    fi
}

# Check pgAdmin
check_pgadmin() {
    echo -n "pgAdmin:           "
    if check_container "budget-tracker-pgadmin"; then
        if curl -f -s http://localhost:5050 > /dev/null 2>&1; then
            print_success "HEALTHY"
            return 0
        else
            print_error "UNHEALTHY"
            return 1
        fi
    else
        print_warning "NOT RUNNING (optional)"
        return 0
    fi
}

# Detailed health report
detailed_health_report() {
    echo ""
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Detailed Health Report${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    # Backend health details
    if check_container "budget-tracker-backend"; then
        print_info "Backend Health Details:"
        curl -s http://localhost:8080/actuator/health | python3 -m json.tool 2>/dev/null || echo "Could not fetch health details"
        echo ""
    fi

    # Database connection pool
    if check_container "budget-tracker-backend"; then
        print_info "Database Connection Pool:"
        curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | python3 -m json.tool 2>/dev/null || echo "Could not fetch metrics"
        echo ""
    fi

    # Container stats
    print_info "Container Resource Usage:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" \
        $(docker ps --filter "name=budget-tracker" --format "{{.Names}}") 2>/dev/null || echo "Could not fetch stats"
    echo ""
}

# Port check
check_ports() {
    echo ""
    print_info "Port Availability:"

    local ports=(5432 6379 9092 8080 9000 9001 3000 9090 5050)
    local port_names=("PostgreSQL" "Redis" "Kafka" "Backend" "MinIO" "MinIO-Console" "Grafana" "Prometheus" "pgAdmin")

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local name=${port_names[$i]}
        echo -n "  Port $port ($name): "

        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_success "IN USE"
        else
            print_warning "NOT IN USE"
        fi
    done
}

# Main health check
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Budget Tracker Health Check${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    local failed=0

    # Core services
    echo -e "${CYAN}Core Services:${NC}"
    check_postgres || ((failed++))
    check_redis || ((failed++))
    check_kafka || ((failed++))
    check_zookeeper || ((failed++))
    check_minio || ((failed++))
    check_backend || ((failed++))

    echo ""
    echo -e "${CYAN}Monitoring & Management:${NC}"
    check_prometheus
    check_grafana
    check_pgadmin

    check_ports

    # Summary
    echo ""
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"

    if [ $failed -eq 0 ]; then
        print_success "All core services are healthy! ✨"
    else
        print_error "$failed service(s) are unhealthy or not running"
        echo ""
        print_info "Troubleshooting tips:"
        print_info "  1. Check logs: docker-compose logs [service-name]"
        print_info "  2. Restart service: docker-compose restart [service-name]"
        print_info "  3. Rebuild: docker-compose up -d --build"
    fi

    echo -e "${CYAN}════════════════════════════════════════════════${NC}"

    # Ask if user wants detailed report
    echo ""
    echo -n "Show detailed health report? (y/n): "
    read -r show_details

    if [ "$show_details" = "y" ]; then
        detailed_health_report
    fi

    exit $failed
}

main
