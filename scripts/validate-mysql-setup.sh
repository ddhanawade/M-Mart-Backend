#!/bin/bash

# MySQL Setup Validation Script for Mahabaleshwer Mart Backend
# This script validates that MySQL is properly configured for all microservices

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default MySQL configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-root}"

# Database names
DATABASES=(
    "mahabaleshwer_mart_users"
    "mahabaleshwer_mart_products"
    "mahabaleshwer_mart_cart"
    "mahabaleshwer_mart_orders"
)

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO")
            echo -e "${BLUE}[INFO]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[SUCCESS]${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}[WARNING]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
    esac
}

# Function to check if MySQL is running
check_mysql_running() {
    print_status "INFO" "Checking if MySQL is running..."
    
    if command -v mysql &> /dev/null; then
        if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT 1;" &> /dev/null; then
            print_status "SUCCESS" "MySQL is running and accessible"
            return 0
        else
            print_status "ERROR" "Cannot connect to MySQL server"
            return 1
        fi
    else
        print_status "ERROR" "MySQL client not found. Please install MySQL client."
        return 1
    fi
}

# Function to check MySQL version
check_mysql_version() {
    print_status "INFO" "Checking MySQL version..."
    
    local version=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT VERSION();" --skip-column-names 2>/dev/null)
    
    if [[ $version == 8.* ]]; then
        print_status "SUCCESS" "MySQL version: $version (Compatible)"
    elif [[ $version == 5.7* ]]; then
        print_status "WARNING" "MySQL version: $version (Compatible, but 8.0+ recommended)"
    else
        print_status "WARNING" "MySQL version: $version (May have compatibility issues)"
    fi
}

# Function to check character set configuration
check_character_set() {
    print_status "INFO" "Checking character set configuration..."
    
    local charset=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'character_set_server';" --skip-column-names 2>/dev/null | cut -f2)
    local collation=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'collation_server';" --skip-column-names 2>/dev/null | cut -f2)
    
    if [[ "$charset" == "utf8mb4" ]]; then
        print_status "SUCCESS" "Character set: $charset (Recommended)"
    else
        print_status "WARNING" "Character set: $charset (utf8mb4 recommended for full Unicode support)"
    fi
    
    if [[ "$collation" == "utf8mb4_unicode_ci" ]] || [[ "$collation" == "utf8mb4_0900_ai_ci" ]]; then
        print_status "SUCCESS" "Collation: $collation (Good)"
    else
        print_status "WARNING" "Collation: $collation (utf8mb4_unicode_ci recommended)"
    fi
}

# Function to check databases
check_databases() {
    print_status "INFO" "Checking required databases..."
    
    for db in "${DATABASES[@]}"; do
        local exists=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$db';" --skip-column-names 2>/dev/null)
        
        if [[ -n "$exists" ]]; then
            print_status "SUCCESS" "Database '$db' exists"
            
            # Check database character set
            local db_charset=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT DEFAULT_CHARACTER_SET_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$db';" --skip-column-names 2>/dev/null)
            
            if [[ "$db_charset" == "utf8mb4" ]]; then
                print_status "SUCCESS" "Database '$db' uses utf8mb4 character set"
            else
                print_status "WARNING" "Database '$db' uses '$db_charset' character set (utf8mb4 recommended)"
            fi
        else
            print_status "ERROR" "Database '$db' does not exist"
        fi
    done
}

# Function to check connection limits
check_connection_limits() {
    print_status "INFO" "Checking connection limits..."
    
    local max_connections=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'max_connections';" --skip-column-names 2>/dev/null | cut -f2)
    local current_connections=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW STATUS LIKE 'Threads_connected';" --skip-column-names 2>/dev/null | cut -f2)
    
    print_status "INFO" "Max connections: $max_connections"
    print_status "INFO" "Current connections: $current_connections"
    
    if [[ $max_connections -ge 151 ]]; then
        print_status "SUCCESS" "Connection limit is adequate for microservices"
    else
        print_status "WARNING" "Connection limit might be low for multiple microservices"
    fi
}

# Function to test connection with application settings
test_application_connections() {
    print_status "INFO" "Testing connections with application-like settings..."
    
    for db in "${DATABASES[@]}"; do
        local service_name=$(echo "$db" | sed 's/mahabaleshwer_mart_//')
        
        # Test connection with typical Spring Boot connection string
        local connection_url="jdbc:mysql://$DB_HOST:$DB_PORT/$db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        
        print_status "INFO" "Testing connection to $service_name service database..."
        
        if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" "$db" -e "SELECT 1 as test_connection;" &> /dev/null; then
            print_status "SUCCESS" "Connection to '$db' successful"
        else
            print_status "ERROR" "Connection to '$db' failed"
        fi
    done
}

# Function to check disk space
check_disk_space() {
    print_status "INFO" "Checking disk space for MySQL data..."
    
    local mysql_datadir=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'datadir';" --skip-column-names 2>/dev/null | cut -f2)
    
    if [[ -n "$mysql_datadir" ]]; then
        local available_space=$(df -h "$mysql_datadir" 2>/dev/null | awk 'NR==2{print $4}')
        print_status "INFO" "Available disk space in MySQL data directory: $available_space"
    else
        print_status "WARNING" "Could not determine MySQL data directory"
    fi
}

# Function to run performance checks
check_performance_settings() {
    print_status "INFO" "Checking performance-related settings..."
    
    local innodb_buffer_pool=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'innodb_buffer_pool_size';" --skip-column-names 2>/dev/null | cut -f2)
    local query_cache_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW VARIABLES LIKE 'query_cache_size';" --skip-column-names 2>/dev/null | cut -f2)
    
    # Convert bytes to MB for buffer pool
    local buffer_pool_mb=$((innodb_buffer_pool / 1024 / 1024))
    
    print_status "INFO" "InnoDB buffer pool size: ${buffer_pool_mb}MB"
    
    if [[ $buffer_pool_mb -ge 128 ]]; then
        print_status "SUCCESS" "InnoDB buffer pool size is adequate"
    else
        print_status "WARNING" "InnoDB buffer pool size might be small for production use"
    fi
}

# Function to generate summary report
generate_summary() {
    print_status "INFO" "=== MySQL Setup Validation Summary ==="
    echo ""
    echo "Configuration:"
    echo "  Host: $DB_HOST"
    echo "  Port: $DB_PORT"
    echo "  Username: $DB_USERNAME"
    echo ""
    echo "Required Databases:"
    for db in "${DATABASES[@]}"; do
        echo "  - $db"
    done
    echo ""
    echo "Next Steps:"
    echo "1. If all checks passed, you can start the microservices"
    echo "2. If databases are missing, run: mysql -u $DB_USERNAME -p < scripts/init-databases.sql"
    echo "3. Start services individually or use docker-compose up"
    echo ""
    echo "Service URLs after startup:"
    echo "  - User Service: http://localhost:8081"
    echo "  - Product Service: http://localhost:8082"
    echo "  - Cart Service: http://localhost:8083"
    echo "  - Order Service: http://localhost:8084"
    echo "  - Notification Service: http://localhost:8085"
}

# Main execution
main() {
    echo ""
    print_status "INFO" "=== Mahabaleshwer Mart MySQL Validation Script ==="
    echo ""
    
    # Run all checks
    check_mysql_running || exit 1
    check_mysql_version
    check_character_set
    check_databases
    check_connection_limits
    test_application_connections
    check_disk_space
    check_performance_settings
    
    echo ""
    generate_summary
    echo ""
    print_status "SUCCESS" "MySQL validation completed!"
}

# Run the main function
main "$@" 