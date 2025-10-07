#!/bin/bash

###############################################################################
# Budget Tracker - Main Deployment Script
#
# This script provides an interactive menu for deploying the Budget Tracker
# application in different environments (local dev, local docker, production)
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="${SCRIPT_DIR}/scripts"

# Banner
print_banner() {
    echo -e "${CYAN}"
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║                                                           ║"
    echo "║         💰  BUDGET TRACKER DEPLOYMENT TOOL  💰           ║"
    echo "║                                                           ║"
    echo "║              Easy deployment for all environments         ║"
    echo "║                                                           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Print functions
print_info() {
    echo -e "${BLUE}ℹ ${NC}$1"
}

print_success() {
    echo -e "${GREEN}✓ ${NC}$1"
}

print_error() {
    echo -e "${RED}✗ ${NC}$1"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${NC}$1"
}

print_step() {
    echo -e "${MAGENTA}▶ ${NC}${BOLD}$1${NC}"
}

# Check if running in GitHub Codespaces
is_codespace() {
    [[ -n "${CODESPACES}" ]] || [[ -n "${CODESPACE_NAME}" ]]
}

# Detect environment
detect_environment() {
    if is_codespace; then
        echo "codespace"
    else
        echo "local"
    fi
}

# Check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."

    local missing_deps=()

    # Check Docker
    if ! command -v docker &> /dev/null; then
        missing_deps+=("docker")
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        missing_deps+=("docker-compose")
    fi

    # Check Java (for local dev)
    if ! command -v java &> /dev/null; then
        print_warning "Java not found (required for local development mode)"
    fi

    # Check Maven (for local dev)
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven not found (required for local development mode)"
    fi

    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing required dependencies: ${missing_deps[*]}"
        print_info "Please install the missing dependencies and try again."
        exit 1
    fi

    print_success "All prerequisites met!"
}

# Make scripts executable
setup_scripts() {
    if [ -d "$SCRIPTS_DIR" ]; then
        chmod +x "$SCRIPTS_DIR"/*.sh 2>/dev/null || true
    fi
}

# Main menu
show_menu() {
    local env=$(detect_environment)

    echo ""
    echo -e "${BOLD}Select deployment mode:${NC}"
    echo ""
    echo "  1) Local Development (Maven + Infrastructure in Docker)"
    echo "  2) Local Docker (Full stack in Docker)"
    echo "  3) Production Deployment"
    echo "  4) Health Check"
    echo "  5) View Logs"
    echo "  6) Database Backup"
    echo "  7) Database Restore"
    echo "  8) Stop All Services"
    echo "  9) Clean Up (Remove all containers and volumes)"
    echo "  0) Exit"
    echo ""

    if [[ "$env" == "codespace" ]]; then
        print_info "Running in GitHub Codespaces"
    fi

    echo -n "Enter choice [0-9]: "
}

# Execute selected option
execute_option() {
    local choice=$1

    case $choice in
        1)
            print_step "Starting Local Development Mode..."
            bash "${SCRIPTS_DIR}/local-dev.sh"
            ;;
        2)
            print_step "Starting Full Docker Stack..."
            bash "${SCRIPTS_DIR}/local-docker.sh"
            ;;
        3)
            print_step "Starting Production Deployment..."
            bash "${SCRIPTS_DIR}/production.sh"
            ;;
        4)
            print_step "Running Health Checks..."
            bash "${SCRIPTS_DIR}/health-check.sh"
            ;;
        5)
            print_step "Viewing Logs..."
            bash "${SCRIPTS_DIR}/logs.sh"
            ;;
        6)
            print_step "Creating Database Backup..."
            bash "${SCRIPTS_DIR}/backup.sh"
            ;;
        7)
            print_step "Restoring Database..."
            bash "${SCRIPTS_DIR}/restore.sh"
            ;;
        8)
            print_step "Stopping All Services..."
            docker-compose down
            print_success "All services stopped"
            ;;
        9)
            print_step "Cleaning Up..."
            bash "${SCRIPTS_DIR}/clean.sh"
            ;;
        0)
            print_info "Exiting..."
            exit 0
            ;;
        *)
            print_error "Invalid option. Please try again."
            ;;
    esac
}

# Main function
main() {
    print_banner
    check_prerequisites
    setup_scripts

    # If scripts directory doesn't exist, create it
    if [ ! -d "$SCRIPTS_DIR" ]; then
        print_error "Scripts directory not found at: $SCRIPTS_DIR"
        print_info "Please ensure all deployment scripts are in the scripts/ directory"
        exit 1
    fi

    while true; do
        show_menu
        read -r choice
        echo ""
        execute_option "$choice"
        echo ""
        echo -e "${CYAN}Press Enter to continue...${NC}"
        read -r
    done
}

# Run main function
main
