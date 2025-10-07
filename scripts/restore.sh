#!/bin/bash

###############################################################################
# Database Restore Script
#
# Restores PostgreSQL database from backup
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

# Check if PostgreSQL container is running
check_postgres() {
    if ! docker ps --format '{{.Names}}' | grep -q "budget-tracker-postgres"; then
        print_error "PostgreSQL container is not running"
        print_info "Start it with: docker-compose up -d postgres"
        exit 1
    fi
}

# List available backups
list_backups() {
    if [ ! -d "$BACKUP_DIR" ]; then
        print_error "Backup directory not found: $BACKUP_DIR"
        exit 1
    fi

    local backups=($(find "$BACKUP_DIR" -name "*.sql.gz" -type f ! -name "latest.sql.gz" | sort -r))

    if [ ${#backups[@]} -eq 0 ]; then
        print_error "No backups found in $BACKUP_DIR"
        exit 1
    fi

    echo -e "${CYAN}Available backups:${NC}"
    echo ""

    local i=1
    for backup in "${backups[@]}"; do
        local size=$(du -h "$backup" | cut -f1)
        local name=$(basename "$backup")
        local timestamp=$(echo "$name" | sed 's/budget-tracker-//' | sed 's/.sql.gz//')

        printf "%2d) %-30s %s\n" $i "$timestamp" "$size"
        ((i++))
    done

    echo ""
}

# Select backup
select_backup() {
    local backups=($(find "$BACKUP_DIR" -name "*.sql.gz" -type f ! -name "latest.sql.gz" | sort -r))

    echo -n "Select backup number (or 'l' for latest, 'q' to quit): "
    read -r selection

    if [ "$selection" = "q" ]; then
        exit 0
    elif [ "$selection" = "l" ]; then
        if [ -L "$BACKUP_DIR/latest.sql.gz" ]; then
            echo "$BACKUP_DIR/latest.sql.gz"
        else
            print_error "Latest backup not found"
            exit 1
        fi
    elif [[ "$selection" =~ ^[0-9]+$ ]] && [ "$selection" -ge 1 ] && [ "$selection" -le "${#backups[@]}" ]; then
        echo "${backups[$((selection-1))]}"
    else
        print_error "Invalid selection"
        exit 1
    fi
}

# Restore backup
restore_backup() {
    local backup_file=$1

    if [ ! -f "$backup_file" ]; then
        print_error "Backup file not found: $backup_file"
        exit 1
    fi

    local backup_name=$(basename "$backup_file")

    echo ""
    print_warning "╔════════════════════════════════════════════════╗"
    print_warning "║           WARNING: DATA WILL BE LOST           ║"
    print_warning "║                                                ║"
    print_warning "║  This will drop the existing database and      ║"
    print_warning "║  restore from backup: $backup_name"
    print_warning "║                                                ║"
    print_warning "╚════════════════════════════════════════════════╝"
    echo ""

    echo -n "Type 'yes' to continue: "
    read -r confirmation

    if [ "$confirmation" != "yes" ]; then
        print_info "Restore cancelled"
        exit 0
    fi

    echo ""
    print_info "Creating safety backup before restore..."

    # Create a safety backup
    local safety_backup="$BACKUP_DIR/pre-restore-$(date +%Y%m%d-%H%M%S).sql.gz"
    if docker exec budget-tracker-postgres pg_dump -U postgres budget_tracker 2>/dev/null | gzip > "$safety_backup"; then
        print_success "Safety backup created: $(basename $safety_backup)"
    else
        print_warning "Could not create safety backup (database might be empty)"
    fi

    # Stop backend to prevent connections
    print_info "Stopping backend application..."
    docker-compose stop backend 2>/dev/null || true

    # Drop and recreate database
    print_info "Dropping existing database..."
    docker exec budget-tracker-postgres psql -U postgres -c "DROP DATABASE IF EXISTS budget_tracker;" 2>/dev/null

    print_info "Creating new database..."
    docker exec budget-tracker-postgres psql -U postgres -c "CREATE DATABASE budget_tracker;" 2>/dev/null

    # Restore from backup
    print_info "Restoring from backup: $backup_name"

    if zcat "$backup_file" | docker exec -i budget-tracker-postgres psql -U postgres budget_tracker > /dev/null 2>&1; then
        print_success "Database restored successfully!"

        # Restart backend
        print_info "Restarting backend application..."
        docker-compose up -d backend 2>/dev/null

        echo ""
        print_success "════════════════════════════════════════════════"
        print_success "  Restore completed successfully!"
        print_success "════════════════════════════════════════════════"
        echo ""
        print_info "Restored from: $backup_name"
        print_info "Safety backup: $(basename $safety_backup)"
    else
        print_error "Restore failed!"
        print_info "Attempting to restore safety backup..."

        zcat "$safety_backup" | docker exec -i budget-tracker-postgres psql -U postgres budget_tracker > /dev/null 2>&1

        print_warning "Rolled back to safety backup"
        docker-compose up -d backend 2>/dev/null

        exit 1
    fi
}

# Restore from custom file
restore_from_file() {
    echo -n "Enter path to backup file (.sql or .sql.gz): "
    read -r custom_file

    if [ ! -f "$custom_file" ]; then
        print_error "File not found: $custom_file"
        exit 1
    fi

    restore_backup "$custom_file"
}

# Main execution
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}  Database Restore${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    check_postgres

    # Check for file argument
    if [ -n "$1" ]; then
        restore_backup "$1"
        exit 0
    fi

    # Interactive mode
    echo "Select restore option:"
    echo "  1) Restore from existing backup"
    echo "  2) Restore from custom file"
    echo "  0) Exit"
    echo ""
    echo -n "Enter choice [0-2]: "
    read -r choice

    case $choice in
        1)
            echo ""
            list_backups
            backup_file=$(select_backup)
            restore_backup "$backup_file"
            ;;
        2)
            echo ""
            restore_from_file
            ;;
        0)
            exit 0
            ;;
        *)
            print_error "Invalid option"
            exit 1
            ;;
    esac
}

main "$@"
