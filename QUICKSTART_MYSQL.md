# Quick Start Guide: Mahabaleshwer Mart with MySQL

This guide will help you quickly set up and run the Mahabaleshwer Mart application with MySQL database.

## Prerequisites ✅

- **Java 17+** installed
- **Maven 3.6+** installed
- **Docker & Docker Compose** (for containerized setup) OR **MySQL 8.0+** (for local setup)
- **Node.js 18+** and **npm** (for frontend)

## Option 1: Docker Compose Setup (Fastest) 🚀

### 1. Start Everything with One Command

```bash
cd mahabaleshwer-mart-backend
docker-compose up -d
```

This will start:
- ✅ MySQL 8.0 database (port 3306)
- ✅ Redis cache (port 6379)  
- ✅ RabbitMQ message broker (ports 5672/15672)
- ✅ All 5 microservices (ports 8081-8085)

### 2. Verify Services are Running

```bash
# Check all containers
docker-compose ps

# Check logs
docker-compose logs -f mysql
docker-compose logs -f user-service
```

### 3. Test the Application

**Backend Health Checks:**
- User Service: http://localhost:8081/actuator/health
- Product Service: http://localhost:8082/actuator/health
- Cart Service: http://localhost:8083/actuator/health
- Order Service: http://localhost:8084/actuator/health
- Notification Service: http://localhost:8085/actuator/health

**Frontend:**
```bash
cd mahabaleshwer-mart
npm install
ng serve
```
- Frontend: http://localhost:4200

### 4. Stop Everything

```bash
docker-compose down        # Stop services
docker-compose down -v     # Stop and remove data
```

## Option 2: Local Setup (More Control) ⚙️

### 1. Install MySQL 8.0

**macOS (Homebrew):**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server-8.0
sudo mysql_secure_installation
```

**Windows:**
Download from [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)

### 2. Create Databases

```bash
# Option A: Run initialization script
mysql -u root -p < mahabaleshwer-mart-backend/scripts/init-databases.sql

# Option B: Manual setup
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_users CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_products CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_cart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_orders CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Validate MySQL Setup

```bash
cd mahabaleshwer-mart-backend
./scripts/validate-mysql-setup.sh
```

### 4. Start Services Individually

```bash
# Terminal 1 - User Service
cd mahabaleshwer-mart-backend/user-service
mvn spring-boot:run

# Terminal 2 - Product Service  
cd mahabaleshwer-mart-backend/product-service
mvn spring-boot:run

# Terminal 3 - Cart Service
cd mahabaleshwer-mart-backend/cart-service
mvn spring-boot:run

# Terminal 4 - Order Service
cd mahabaleshwer-mart-backend/order-service
mvn spring-boot:run

# Terminal 5 - Notification Service
cd mahabaleshwer-mart-backend/notification-service
mvn spring-boot:run

# Terminal 6 - Frontend
cd mahabaleshwer-mart
ng serve
```

## Configuration 🔧

### Environment Variables

```bash
# Database Configuration
export DB_USERNAME=root
export DB_PASSWORD=root
export DB_HOST=localhost
export DB_PORT=3306

# Optional: Redis & RabbitMQ (if running locally)
export REDIS_HOST=localhost
export REDIS_PORT=6379
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=admin
export RABBITMQ_PASSWORD=admin123
```

### Database URLs

The application will connect to these databases:

- **User Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_users`
- **Product Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_products`
- **Cart Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_cart`
- **Order Service**: `jdbc:mysql://localhost:3306/mahabaleshwer_mart_orders`

## Key Features 🌟

### What's Changed from PostgreSQL

✅ **Database**: MySQL 8.0 instead of PostgreSQL  
✅ **Driver**: `mysql-connector-java` instead of `postgresql`  
✅ **Dialect**: `MySQL8Dialect` instead of `PostgreSQLDialect`  
✅ **Character Set**: UTF8MB4 for full Unicode support  
✅ **Connection Pooling**: Optimized HikariCP settings  

### Database Schema

- **Auto-creation**: Tables created automatically on first run
- **Character Set**: UTF8MB4 (supports emojis and all Unicode)
- **Collation**: `utf8mb4_unicode_ci` (case-insensitive)
- **Timezone**: UTC for consistency

## Testing the Integration 🧪

### 1. Register a New User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com", 
    "password": "password123",
    "phone": "+1234567890"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Get Products
```bash
curl -X GET http://localhost:8082/api/products?page=0&size=10
```

### 4. Frontend Testing
- Visit http://localhost:4200
- Register/Login through the UI
- Browse products
- Add items to cart
- Complete checkout process

## Troubleshooting 🔍

### Common Issues

#### MySQL Connection Error
```
Error: Connection refused
```
**Solution:**
```bash
# Check if MySQL is running
mysql -u root -p -e "SELECT 1;"

# Start MySQL if needed
brew services start mysql  # macOS
sudo systemctl start mysql  # Linux
```

#### Database Not Found
```
Error: Unknown database 'mahabaleshwer_mart_users'
```
**Solution:**
```bash
mysql -u root -p < scripts/init-databases.sql
```

#### Authentication Failed
```
Error: Access denied for user 'root'
```
**Solution:**
```bash
# Reset password
mysql_secure_installation

# Or create new user
mysql -u root -p
CREATE USER 'appuser'@'%' IDENTIFIED BY 'newpassword';
GRANT ALL PRIVILEGES ON mahabaleshwer_mart_*.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```

#### Port Already in Use
```
Error: Port 3306 already in use
```
**Solution:**
```bash
# Find process using port
lsof -i :3306

# Kill process or change port in application.yml
```

### Validation Script

Run the validation script to check your setup:

```bash
cd mahabaleshwer-mart-backend
./scripts/validate-mysql-setup.sh
```

## Database Management 📊

### Backup
```bash
mysqldump -u root -p --databases \
  mahabaleshwer_mart_users \
  mahabaleshwer_mart_products \
  mahabaleshwer_mart_cart \
  mahabaleshwer_mart_orders \
  > backup.sql
```

### Restore
```bash
mysql -u root -p < backup.sql
```

### Monitor
```sql
-- Check database sizes
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema LIKE 'mahabaleshwer_mart_%'
GROUP BY table_schema;

-- Check connections
SHOW PROCESSLIST;
```

## Architecture Overview 🏗️

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │
│   Angular       │◄──►│   (Optional)    │
│   Port: 4200    │    │   Port: 8080    │
└─────────────────┘    └─────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
┌──────▼──────┐    ┌──────────▼──────┐    ┌──────────▼──────┐
│ User Service│    │Product Service  │    │ Cart Service    │
│ Port: 8081  │    │ Port: 8082      │    │ Port: 8083      │
└──────┬──────┘    └─────────┬───────┘    └─────────┬───────┘
       │                     │                      │
       │           ┌─────────▼───────┐              │
       │           │ Order Service   │              │
       │           │ Port: 8084      │              │
       │           └─────────┬───────┘              │
       │                     │                      │
       │           ┌─────────▼────────┐             │
       │           │Notification      │             │
       │           │Service           │             │
       │           │Port: 8085        │             │
       │           └──────────────────┘             │
       │                                            │
       └───────────────────┬────────────────────────┘
                           │
                  ┌────────▼────────┐
                  │     MySQL       │
                  │   Port: 3306    │
                  │                 │
                  │ ┌─────────────┐ │
                  │ │   Users DB  │ │
                  │ │ Products DB │ │
                  │ │   Cart DB   │ │
                  │ │  Orders DB  │ │
                  │ └─────────────┘ │
                  └─────────────────┘
```

## What's Next? 🚀

1. **Production Setup**: Review [MYSQL_SETUP.md](MYSQL_SETUP.md) for production configuration
2. **Security**: Configure SSL, create dedicated users, set up firewalls
3. **Monitoring**: Set up monitoring for MySQL and application metrics
4. **Performance**: Tune MySQL configuration based on load testing
5. **Backup Strategy**: Implement automated backups and disaster recovery

## Support 💬

- 📚 **Documentation**: [MYSQL_SETUP.md](MYSQL_SETUP.md)
- 🔧 **CORS Setup**: [CORS_CONFIGURATION.md](CORS_CONFIGURATION.md)  
- 🎯 **Frontend Integration**: [../mahabaleshwer-mart/FRONTEND_BACKEND_INTEGRATION.md](../mahabaleshwer-mart/FRONTEND_BACKEND_INTEGRATION.md)

## Success! ✨

If everything is working correctly, you should see:

✅ All microservices running and healthy  
✅ MySQL databases created and connected  
✅ Frontend loading and communicating with backend  
✅ User registration/login working  
✅ Product catalog loading  
✅ Cart operations working  
✅ Order placement functional  

**Congratulations! Your Mahabaleshwer Mart application is now running with MySQL! 🎉** 