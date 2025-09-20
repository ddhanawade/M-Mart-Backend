# MySQL Database Setup for Mahabaleshwer Mart Backend

This document provides comprehensive instructions for setting up and configuring MySQL database for all Mahabaleshwer Mart microservices.

## Overview

The application now uses MySQL 8.0 instead of PostgreSQL for all database operations. Each microservice has its own dedicated database:

- **User Service**: `mahabaleshwer_mart_users`
- **Product Service**: `mahabaleshwer_mart_products`
- **Cart Service**: `mahabaleshwer_mart_cart`
- **Order Service**: `mahabaleshwer_mart_orders`

## Prerequisites

- MySQL 8.0 or higher
- Docker and Docker Compose (for containerized setup)
- Java 17+ (for running microservices)

## Setup Options

### Option 1: Docker Compose Setup (Recommended)

This is the easiest way to get started with the complete application stack.

#### 1. Start the Full Application Stack

```bash
cd mahabaleshwer-mart-backend
docker-compose up -d
```

This will start:
- MySQL 8.0 database server on port 3306
- Redis cache on port 6379
- RabbitMQ message broker on ports 5672/15672
- All microservices on their respective ports (8081-8085)

#### 2. Check Container Status

```bash
docker-compose ps
```

#### 3. View Logs

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f mysql
docker-compose logs -f user-service
```

#### 4. Stop the Application

```bash
docker-compose down
```

#### 5. Clean Up (Remove all data)

```bash
docker-compose down -v
```

### Option 2: Local MySQL Installation

If you prefer to run MySQL locally and microservices separately.

#### 1. Install MySQL 8.0

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server-8.0
sudo mysql_secure_installation
```

**macOS (using Homebrew):**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

**Windows:**
Download and install from [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)

#### 2. Configure MySQL

```sql
-- Connect to MySQL
mysql -u root -p

-- Create databases
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_users 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_products 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_cart 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_orders 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create application user (optional, for production)
CREATE USER IF NOT EXISTS 'mahabaleshwer_user'@'%' IDENTIFIED BY 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON mahabaleshwer_mart_users.* TO 'mahabaleshwer_user'@'%';
GRANT ALL PRIVILEGES ON mahabaleshwer_mart_products.* TO 'mahabaleshwer_user'@'%';
GRANT ALL PRIVILEGES ON mahabaleshwer_mart_cart.* TO 'mahabaleshwer_user'@'%';
GRANT ALL PRIVILEGES ON mahabaleshwer_mart_orders.* TO 'mahabaleshwer_user'@'%';

FLUSH PRIVILEGES;
```

#### 3. Update Application Configuration

If using custom database credentials, update the environment variables:

```bash
export DB_USERNAME=mahabaleshwer_user
export DB_PASSWORD=secure_password
```

#### 4. Run Database Initialization Script

```bash
mysql -u root -p < scripts/init-databases.sql
```

#### 5. Start Individual Services

```bash
# Terminal 1 - User Service
cd user-service
mvn spring-boot:run

# Terminal 2 - Product Service
cd product-service
mvn spring-boot:run

# Terminal 3 - Cart Service
cd cart-service
mvn spring-boot:run

# Terminal 4 - Order Service
cd order-service
mvn spring-boot:run

# Terminal 5 - Notification Service
cd notification-service
mvn spring-boot:run
```

## Database Configuration Details

### Connection URLs

- **User Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_users`
- **Product Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_products`
- **Cart Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_cart`
- **Order Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_orders`

### Important URL Parameters

- `createDatabaseIfNotExist=true`: Automatically creates the database if it doesn't exist
- `useSSL=false`: Disables SSL for local development
- `allowPublicKeyRetrieval=true`: Allows public key retrieval for authentication
- `serverTimezone=UTC`: Sets the server timezone to UTC

### Character Set and Collation

- **Character Set**: `utf8mb4` (supports full UTF-8, including emojis)
- **Collation**: `utf8mb4_unicode_ci` (case-insensitive Unicode collation)

## Environment Variables

The following environment variables can be used to configure database connections:

```bash
# Database Configuration
DB_USERNAME=root                    # Default: root
DB_PASSWORD=root                    # Default: root
DB_HOST=localhost                   # Default: localhost
DB_PORT=3306                       # Default: 3306

# Redis Configuration
REDIS_HOST=localhost               # Default: localhost
REDIS_PORT=6379                    # Default: 6379

# RabbitMQ Configuration (for Order Service)
RABBITMQ_HOST=localhost            # Default: localhost
RABBITMQ_PORT=5672                 # Default: 5672
RABBITMQ_USERNAME=admin            # Default: admin
RABBITMQ_PASSWORD=admin123         # Default: admin123
```

## Database Schema Management

### Hibernate DDL Settings

All services are configured with `hibernate.ddl-auto=update`, which means:
- Tables are automatically created on first run
- Schema changes are automatically applied
- No manual schema management required for development

### For Production

For production environments, consider:
1. Setting `hibernate.ddl-auto=validate`
2. Using Flyway or Liquibase for schema migrations
3. Creating separate database users with limited privileges

## Monitoring and Maintenance

### Health Checks

Each service exposes health check endpoints:
- User Service: `http://localhost:8081/actuator/health`
- Product Service: `http://localhost:8082/actuator/health`
- Cart Service: `http://localhost:8083/actuator/health`
- Order Service: `http://localhost:8084/actuator/health`

### Database Health Check

```sql
-- Check database connections
SHOW PROCESSLIST;

-- Check database sizes
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema IN (
    'mahabaleshwer_mart_users',
    'mahabaleshwer_mart_products',
    'mahabaleshwer_mart_cart',
    'mahabaleshwer_mart_orders'
)
GROUP BY table_schema;
```

### Backup and Restore

#### Backup
```bash
# Backup all databases
mysqldump -u root -p --databases \
  mahabaleshwer_mart_users \
  mahabaleshwer_mart_products \
  mahabaleshwer_mart_cart \
  mahabaleshwer_mart_orders \
  > mahabaleshwer_mart_backup.sql

# Backup specific database
mysqldump -u root -p mahabaleshwer_mart_users > users_backup.sql
```

#### Restore
```bash
# Restore from backup
mysql -u root -p < mahabaleshwer_mart_backup.sql

# Restore specific database
mysql -u root -p mahabaleshwer_mart_users < users_backup.sql
```

## Performance Optimization

### MySQL Configuration Tuning

Add these settings to your MySQL configuration file (`my.cnf` or `my.ini`):

```ini
[mysqld]
# Buffer pool size (adjust based on available RAM)
innodb_buffer_pool_size = 1G

# Connection settings
max_connections = 200
wait_timeout = 600

# Query cache (if using MySQL < 8.0)
query_cache_type = 1
query_cache_size = 256M

# Binary logging
log_bin = mysql-bin
binlog_format = ROW

# Character set
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
```

### Connection Pool Settings

The application uses HikariCP with these settings:
- Maximum pool size: 20 connections per service
- Minimum idle: 5 connections
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

## Troubleshooting

### Common Issues

#### 1. Connection Refused Error
```
Error: Connection refused to MySQL server
```
**Solution**: Ensure MySQL is running and accessible on port 3306.

#### 2. Authentication Failed
```
Error: Access denied for user 'root'@'localhost'
```
**Solution**: Check username/password and ensure user has proper privileges.

#### 3. Database Not Found
```
Error: Unknown database 'mahabaleshwer_mart_users'
```
**Solution**: Run the database initialization script.

#### 4. Character Set Issues
```
Error: Incorrect string value for column
```
**Solution**: Ensure database uses UTF8MB4 character set.

### Debug Commands

```sql
-- Check database existence
SHOW DATABASES LIKE 'mahabaleshwer_mart_%';

-- Check tables in a database
USE mahabaleshwer_mart_users;
SHOW TABLES;

-- Check character sets
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';

-- Check current connections
SHOW PROCESSLIST;

-- Check user privileges
SHOW GRANTS FOR 'root'@'localhost';
```

## Security Considerations

### Development Environment
- Default credentials are used for simplicity
- SSL is disabled for local development
- All privileges are granted to development users

### Production Environment
- Use strong, unique passwords
- Enable SSL/TLS encryption
- Create separate users with minimal required privileges
- Enable audit logging
- Regular security updates
- Network security (firewall, VPN)
- Regular backups

## Migration from PostgreSQL

If you're migrating from PostgreSQL to MySQL:

1. Export data from PostgreSQL
2. Transform data types as needed
3. Update application configuration
4. Import data to MySQL
5. Test all functionality

### Data Type Mapping

| PostgreSQL | MySQL |
|------------|--------|
| SERIAL | AUTO_INCREMENT |
| TEXT | TEXT or LONGTEXT |
| TIMESTAMP | TIMESTAMP |
| BOOLEAN | BOOLEAN or TINYINT(1) |
| UUID | CHAR(36) or BINARY(16) |

## Additional Resources

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [Spring Boot MySQL Configuration](https://spring.io/guides/gs/accessing-data-mysql/)
- [Docker MySQL Documentation](https://hub.docker.com/_/mysql)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP) 