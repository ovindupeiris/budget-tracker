#!/bin/bash

###############################################################################
# Cleanup Script
#
# Removes containers, volumes, and data for fresh start
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

# Stop all services
stop_services() {
    print_info "Stopping all services..."
    cd "$PROJECT_ROOT"

    docker-compose down 2>/dev/null || true
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml down 2>/dev/null || true

    print_success "Services stopped"
}

# Remove containers
remove_containers() {
    print_info "Removing containers..."

    local containers=$(docker ps -a --filter "name=budget-tracker" --format "{{.Names}}")

    if [ -z "$containers" ]; then
        print_info "No containers to remove"
        return
    fi

    echo "$containers" | xargs docker rm -f 2>/dev/null || true
    print_success "Containers removed"
}

# Remove volumes
remove_volumes() {
    print_info "Removing volumes..."

    cd "$PROJECT_ROOT"
    docker-compose down -v 2>/dev/null || true

    # Remove named volumes
    local volumes=$(docker volume ls --filter "name=budget-tracker" --format "{{.Name}}")

    if [ -z "$volumes" ]; then
        print_info "No volumes to remove"
        return
    fi

    echo "$volumes" | xargs docker volume rm -f 2>/dev/null || true
    print_success "Volumes removed"
}

# Remove images
remove_images() {
    print_info "Removing images..."

    local images=$(docker images --filter "reference=*budget-tracker*" --format "{{.Repository}}:{{.Tag}}")

    if [ -z "$images" ]; then
        print_info "No images to remove"
        return
    fi

    echo "$images" | xargs docker rmi -f 2>/dev/null || true
    print_success "Images removed"
}

# Remove networks
remove_networks() {
    print_info "Removing networks..."

    local networks=$(docker network ls --filter "name=budget-tracker" --format "{{.Name}}")

    if [ -z "$networks" ]; then
        print_info "No networks to remove"
        return
    fi

    echo "$networks" | xargs docker network rm 2>/dev/null || true
    print_success "Networks removed"
}

# Remove build artifacts
remove_build_artifacts() {
    print_info "Removing build artifacts..."

    cd "$PROJECT_ROOT"

    # Remove backend target directory
    if [ -d "backend/target" ]; then
        rm -rf backend/target
        print_success "Removed backend/target"
    fi

    # Remove logs
    if [ -d "backend/logs" ]; then
        rm -rf backend/logs/*
        print_success "Cleared backend logs"
    fi

    print_success "Build artifacts removed"
}

# Remove logs
remove_logs() {
    print_info "Removing log files..."

    if [ -d "$PROJECT_ROOT/logs" ]; then
        rm -rf "$PROJECT_ROOT/logs"
        print_success "Logs removed"
    fi
}

# System prune
system_prune() {
    print_info "Running Docker system prune..."

    docker system prune -f --volumes 2>/dev/null

    print_success "System prune completed"
}

# Full cleanup
full_cleanup() {
    print_warning "╔════════════════════════════════════════════════╗"
    print_warning "║             FULL CLEANUP WARNING               ║"
    print_warning "║                                                ║"
    print_warning "║  This will remove ALL data including:          ║"
    print_warning "║  - All containers                              ║"
    print_warning "║  - All volumes (DATABASE DATA WILL BE LOST!)   ║"
    print_warning "║  - All Docker images                           ║"
    print_warning "║  - All networks                                ║"
    print_warning "║  - Build artifacts                             ║"
    print_warning "║  - Log files                                   ║"
    print_warning "║                                                ║"
    print_warning "╚════════════════════════════════════════════════╝"
    echo ""

    echo -n "Type 'DELETE' to confirm full cleanup: "
    read -r confirmation

    if [ "$confirmation" != "DELETE" ]; then
        print_info "Cleanup cancelled"
        exit 0
    fi

    echo ""

    stop_services
    remove_containers
    remove_volumes
    remove_images
    remove_networks
    remove_build_artifacts
    remove_logs

    print_success "════════════════════════════════════════════════"
    print_success "  Full cleanup completed!"
    print_success "════════════════════════════════════════════════"
}

# Light cleanup (keep data)
light_cleanup() {
    print_info "Performing light cleanup (keeping data)..."
    echo ""

    stop_services
    remove_containers
    remove_build_artifacts

    print_success "════════════════════════════════════════════════"
    print_success "  Light cleanup completed!"
    print_success "════════════════════════════════════════════════"
    print_info "Data volumes preserved"
}

# Main menu
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Cleanup Tool${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    echo "Select cleanup level:"
    echo ""
    echo "  1) Light cleanup (remove containers, keep data)"
    echo "  2) Full cleanup (remove everything including data)"
    echo "  3) Remove containers only"
    echo "  4) Remove volumes only"
    echo "  5) Remove images only"
    echo "  6) Remove build artifacts"
    echo "  7) Docker system prune"
    echo "  0) Exit"
    echo ""
    echo -n "Enter choice [0-7]: "
    read -r choice

    echo ""

    case $choice in
        1)
            light_cleanup
            ;;
        2)
            full_cleanup
            ;;
        3)
            stop_services
            remove_containers
            ;;
        4)
            stop_services
            remove_volumes
            ;;
        5)
            remove_images
            ;;
        6)
            remove_build_artifacts
            ;;
        7)
            system_prune
            ;;
        0)
            exit 0
            ;;
        *)
            print_error "Invalid option"
            exit 1
            ;;
    esac

    echo ""
    print_info "You can now start fresh with: ./deploy.sh"
}

main
