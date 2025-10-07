#!/bin/bash

###############################################################################
# Database Backup Script
#
# Creates backups of PostgreSQL database
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
BACKUP_DIR="$PROJECT_ROOT/backups"

print_info() { echo -e "${BLUE}ℹ ${NC}$1"; }
print_success() { echo -e "${GREEN}✓ ${NC}$1"; }
print_error() { echo -e "${RED}✗ ${NC}$1"; }
print_warning() { echo -e "${YELLOW}⚠ ${NC}$1"; }

# Create backup directory
setup_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        print_success "Created backup directory: $BACKUP_DIR"
    fi
}

# Check if PostgreSQL container is running
check_postgres() {
    if ! docker ps --format '{{.Names}}' | grep -q "budget-tracker-postgres"; then
        print_error "PostgreSQL container is not running"
        print_info "Start it with: docker-compose up -d postgres"
        exit 1
    fi
}

# Create backup
create_backup() {
    local timestamp=$(date +%Y%m%d-%H%M%S)
    local backup_file="$BACKUP_DIR/budget-tracker-${timestamp}.sql"
    local compressed_file="${backup_file}.gz"

    print_info "Creating database backup..."

    # Dump database
    if docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker > "$backup_file" 2>/dev/null; then
        print_success "Database dumped successfully"

        # Compress backup
        print_info "Compressing backup..."
        gzip "$backup_file"

        local file_size=$(du -h "$compressed_file" | cut -f1)
        print_success "Backup created: ${compressed_file}"
        print_info "Size: $file_size"

        # Create latest symlink
        ln -sf "$(basename $compressed_file)" "$BACKUP_DIR/latest.sql.gz"
        print_success "Latest backup link updated"

        return 0
    else
        print_error "Failed to create backup"
        rm -f "$backup_file" 2>/dev/null
        return 1
    fi
}

# List backups
list_backups() {
    echo ""
    print_info "Available backups:"
    echo ""

    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A $BACKUP_DIR/*.sql.gz 2>/dev/null)" ]; then
        print_warning "No backups found"
        return
    fi

    echo -e "${CYAN}Timestamp           Size    File${NC}"
    echo "─────────────────────────────────────────────────────"

    for backup in "$BACKUP_DIR"/*.sql.gz; do
        if [ -f "$backup" ] && [ "$(basename "$backup")" != "latest.sql.gz" ]; then
            local size=$(du -h "$backup" | cut -f1)
            local name=$(basename "$backup")
            local timestamp=$(echo "$name" | sed 's/budget-tracker-//' | sed 's/.sql.gz//')

            printf "%-20s%-8s%s\n" "$timestamp" "$size" "$name"
        fi
    done

    echo ""
    print_info "Backups location: $BACKUP_DIR"
}

# Cleanup old backups
cleanup_old_backups() {
    local keep_days=${1:-30}

    print_info "Cleaning up backups older than $keep_days days..."

    local deleted=0
    while IFS= read -r backup; do
        rm -f "$backup"
        ((deleted++))
    done < <(find "$BACKUP_DIR" -name "*.sql.gz" -type f -mtime +$keep_days)

    if [ $deleted -gt 0 ]; then
        print_success "Deleted $deleted old backup(s)"
    else
        print_info "No old backups to delete"
    fi
}

# Automated backup
automated_backup() {
    print_info "Setting up automated daily backups..."

    local cron_job="0 2 * * * $SCRIPT_DIR/backup.sh --auto"

    # Check if cron job already exists
    if crontab -l 2>/dev/null | grep -q "$SCRIPT_DIR/backup.sh"; then
        print_warning "Automated backup is already configured"
    else
        (crontab -l 2>/dev/null; echo "$cron_job") | crontab -
        print_success "Automated daily backup configured (runs at 2 AM)"
    fi
}

# Main execution
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Database Backup${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    setup_backup_dir
    check_postgres

    # Check for auto flag
    if [ "$1" = "--auto" ]; then
        create_backup
        cleanup_old_backups 30
        exit 0
    fi

    # Interactive menu
    echo "Select an option:"
    echo "  1) Create new backup"
    echo "  2) List all backups"
    echo "  3) Setup automated backups"
    echo "  4) Cleanup old backups"
    echo "  0) Exit"
    echo ""
    echo -n "Enter choice [0-4]: "
    read -r choice

    case $choice in
        1)
            echo ""
            create_backup
            echo ""
            list_backups
            ;;
        2)
            list_backups
            ;;
        3)
            echo ""
            automated_backup
            ;;
        4)
            echo ""
            echo -n "Keep backups for how many days? [30]: "
            read -r days
            days=${days:-30}
            cleanup_old_backups $days
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
    print_success "Done!"
}

main "$@"
