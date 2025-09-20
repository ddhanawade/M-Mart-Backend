-- Database initialization script for Mahabaleshwer Mart microservices
-- This script creates separate databases for each microservice

-- Create databases
CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_users 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_products 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_carts 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mahabaleshwer_mart_orders 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create users for each service (optional, for production use)
-- CREATE USER IF NOT EXISTS 'user_service_user'@'%' IDENTIFIED BY 'user_pass';
-- CREATE USER IF NOT EXISTS 'product_service_user'@'%' IDENTIFIED BY 'product_pass';
-- CREATE USER IF NOT EXISTS 'cart_service_user'@'%' IDENTIFIED BY 'cart_pass';
-- CREATE USER IF NOT EXISTS 'order_service_user'@'%' IDENTIFIED BY 'order_pass';

-- Grant privileges
-- GRANT ALL PRIVILEGES ON mahabaleshwer_mart_users.* TO 'user_service_user'@'%';
-- GRANT ALL PRIVILEGES ON mahabaleshwer_mart_products.* TO 'product_service_user'@'%';
-- GRANT ALL PRIVILEGES ON mahabaleshwer_mart_cart.* TO 'cart_service_user'@'%';
-- GRANT ALL PRIVILEGES ON mahabaleshwer_mart_orders.* TO 'order_service_user'@'%';

-- Flush privileges to apply changes
-- FLUSH PRIVILEGES;

-- For development, we'll use the default root user for all services 