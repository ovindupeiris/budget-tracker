#!/bin/bash

###############################################################################
# Database Initialization Script
#
# Creates and initializes the database if it doesn't exist
# Safe to run multiple times - only creates if missing
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

print_info() { echo -e "${BLUE}ℹ ${NC}$1"; }
print_success() { echo -e "${GREEN}✓ ${NC}$1"; }
print_error() { echo -e "${RED}✗ ${NC}$1"; }
print_warning() { echo -e "${YELLOW}⚠ ${NC}$1"; }

# Database configuration
DB_NAME=${DB_NAME:-budget_tracker}
DB_USER=${DB_USERNAME:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}
POSTGRES_CONTAINER="budget-tracker-postgres"

# Check if PostgreSQL container is running
check_postgres_running() {
    if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
        print_error "PostgreSQL container is not running"
        print_info "Please start infrastructure first"
        return 1
    fi
    return 0
}

# Wait for PostgreSQL to be ready
wait_for_postgres() {
    print_info "Waiting for PostgreSQL to be ready..."

    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if docker exec "$POSTGRES_CONTAINER" pg_isready -U postgres > /dev/null 2>&1; then
            print_success "PostgreSQL is ready!"
            return 0
        fi

        sleep 1
        attempt=$((attempt + 1))
        echo -n "."
    done

    echo ""
    print_error "PostgreSQL did not become ready in time"
    return 1
}

# Check if database exists
database_exists() {
    local db_name=$1

    local result=$(docker exec "$POSTGRES_CONTAINER" psql -U postgres -tAc \
        "SELECT 1 FROM pg_database WHERE datname='$db_name'" 2>/dev/null)

    if [ "$result" = "1" ]; then
        return 0
    else
        return 1
    fi
}

# Create database
create_database() {
    local db_name=$1

    print_info "Creating database: $db_name"

    if docker exec "$POSTGRES_CONTAINER" psql -U postgres -c \
        "CREATE DATABASE $db_name;" > /dev/null 2>&1; then
        print_success "Database '$db_name' created successfully"
        return 0
    else
        print_error "Failed to create database '$db_name'"
        return 1
    fi
}

# Create extensions
create_extensions() {
    local db_name=$1

    print_info "Creating database extensions..."

    # Create UUID extension
    docker exec "$POSTGRES_CONTAINER" psql -U postgres -d "$db_name" -c \
        "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";" > /dev/null 2>&1

    # Create pg_trgm for text search
    docker exec "$POSTGRES_CONTAINER" psql -U postgres -d "$db_name" -c \
        "CREATE EXTENSION IF NOT EXISTS pg_trgm;" > /dev/null 2>&1

    print_success "Extensions created"
}

# Verify database tables (after Flyway migration)
check_database_tables() {
    local db_name=$1

    local table_count=$(docker exec "$POSTGRES_CONTAINER" psql -U postgres -d "$db_name" -tAc \
        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null)

    if [ -n "$table_count" ] && [ "$table_count" -gt 0 ]; then
        print_info "Database has $table_count tables"
        return 0
    else
        print_warning "Database exists but has no tables (migrations will run on app startup)"
        return 0
    fi
}

# Main initialization
initialize_database() {
    print_info "Initializing database: $DB_NAME"
    echo ""

    # Check PostgreSQL is running
    if ! check_postgres_running; then
        return 1
    fi

    # Wait for PostgreSQL
    if ! wait_for_postgres; then
        return 1
    fi

    echo ""

    # Check if database exists
    if database_exists "$DB_NAME"; then
        print_success "Database '$DB_NAME' already exists"
        check_database_tables "$DB_NAME"
        print_info "Database is ready to use"
        return 0
    fi

    # Create database
    if ! create_database "$DB_NAME"; then
        return 1
    fi

    # Create extensions
    create_extensions "$DB_NAME"

    echo ""
    print_success "════════════════════════════════════════════════"
    print_success "  Database initialized successfully!"
    print_success "════════════════════════════════════════════════"
    print_info "Database: $DB_NAME"
    print_info "User: $DB_USER"
    print_info "Migrations will run automatically on app startup"
    echo ""

    return 0
}

# Run initialization
main() {
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}    Database Initialization${NC}"
    echo -e "${CYAN}════════════════════════════════════════════════${NC}"
    echo ""

    initialize_database
    exit_code=$?

    if [ $exit_code -ne 0 ]; then
        echo ""
        print_error "Database initialization failed"
        print_info "Check the logs with: docker logs $POSTGRES_CONTAINER"
    fi

    return $exit_code
}

# Allow script to be sourced or run directly
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main
fi
