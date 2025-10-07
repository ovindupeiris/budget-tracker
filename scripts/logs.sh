#!/bin/bash

###############################################################################
# Logs Viewer Script
#
# Interactive log viewer for all services
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

# View all logs
view_all_logs() {
    print_info "Viewing logs from all services (Ctrl+C to exit)"
    echo ""
    docker-compose logs -f --tail=100
}

# View backend logs
view_backend_logs() {
    print_info "Viewing backend logs (Ctrl+C to exit)"
    echo ""
    docker-compose logs -f --tail=100 backend
}

# View database logs
view_database_logs() {
    print_info "Viewing PostgreSQL logs (Ctrl+C to exit)"
    echo ""
    docker-compose logs -f --tail=100 postgres
}

# View redis logs
view_redis_logs() {
    print_info "Viewing Redis logs (Ctrl+C to exit)"
    echo ""
    docker-compose logs -f --tail=100 redis
}

# View kafka logs
view_kafka_logs() {
    print_info "Viewing Kafka logs (Ctrl+C to exit)"
    echo ""
    docker-compose logs -f --tail=100 kafka
}

# View specific service logs
view_service_logs() {
    echo -n "Enter service name (backend, postgres, redis, kafka, etc.): "
    read -r service

    if docker-compose ps "$service" > /dev/null 2>&1; then
        print_info "Viewing $service logs (Ctrl+C to exit)"
        echo ""
        docker-compose logs -f --tail=100 "$service"
    else
        print_error "Service not found: $service"
    fi
}

# Search logs
search_logs() {
    echo -n "Enter search term: "
    read -r search_term

    echo -n "Enter service name (or 'all' for all services): "
    read -r service

    print_info "Searching for: $search_term"
    echo ""

    if [ "$service" = "all" ]; then
        docker-compose logs --tail=1000 | grep -i "$search_term" --color=always
    else
        docker-compose logs --tail=1000 "$service" | grep -i "$search_term" --color=always
    fi
}

# Export logs
export_logs() {
    local timestamp=$(date +%Y%m%d-%H%M%S)
    local export_dir="$PROJECT_ROOT/logs/exports"
    mkdir -p "$export_dir"

    print_info "Exporting logs..."

    # Export all service logs
    for service in backend postgres redis kafka zookeeper minio; do
        if docker-compose ps "$service" > /dev/null 2>&1; then
            local log_file="$export_dir/${service}-${timestamp}.log"
            docker-compose logs --tail=5000 "$service" > "$log_file" 2>&1
            print_success "Exported: ${service}-${timestamp}.log"
        fi
    done

    # Create archive
    cd "$export_dir"
    local archive_name="logs-${timestamp}.tar.gz"
    tar -czf "$archive_name" *-${timestamp}.log 2>/dev/null
    rm -f *-${timestamp}.log

    print_success "Logs exported to: $export_dir/$archive_name"
}

# Show error logs only
view_errors() {
    print_info "Showing recent errors from all services"
    echo ""

    docker-compose logs --tail=500 | grep -iE "error|exception|fatal|failed" --color=always || print_info "No recent errors found"
}

# Tail logs with filter
tail_with_filter() {
    echo "Common filters:"
    echo "  1) Errors only"
    echo "  2) Warnings only"
    echo "  3) HTTP requests"
    echo "  4) Database queries"
    echo "  5) Custom pattern"
    echo ""
    echo -n "Select filter [1-5]: "
    read -r filter_choice

    local pattern=""
    case $filter_choice in
        1) pattern="error|exception|fatal" ;;
        2) pattern="warn|warning" ;;
        3) pattern="HTTP|GET|POST|PUT|DELETE|PATCH" ;;
        4) pattern="SQL|SELECT|INSERT|UPDATE|DELETE|Hibernate" ;;
        5)
            echo -n "Enter custom pattern: "
            read -r pattern
            ;;
        *)
            print_error "Invalid choice"
            return
            ;;
    esac

    print_info "Filtering logs for: $pattern"
    echo ""
    docker-compose logs -f --tail=100 | grep -iE "$pattern" --color=always
}

# Main menu
show_menu() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Logs Viewer${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""
    echo "Select an option:"
    echo ""
    echo "  1) View all logs (live)"
    echo "  2) View backend logs"
    echo "  3) View database logs"
    echo "  4) View Redis logs"
    echo "  5) View Kafka logs"
    echo "  6) View specific service"
    echo "  7) Search logs"
    echo "  8) View errors only"
    echo "  9) Tail with filter"
    echo " 10) Export logs to file"
    echo "  0) Exit"
    echo ""
    echo -n "Enter choice [0-10]: "
}

# Main execution
main() {
    cd "$PROJECT_ROOT"

    while true; do
        show_menu
        read -r choice
        echo ""

        case $choice in
            1) view_all_logs ;;
            2) view_backend_logs ;;
            3) view_database_logs ;;
            4) view_redis_logs ;;
            5) view_kafka_logs ;;
            6) view_service_logs ;;
            7) search_logs ;;
            8) view_errors ;;
            9) tail_with_filter ;;
            10) export_logs ;;
            0)
                print_info "Exiting..."
                exit 0
                ;;
            *)
                print_error "Invalid option"
                ;;
        esac

        echo ""
        echo -e "${CYAN}Press Enter to continue...${NC}"
        read -r
        clear
    done
}

main
