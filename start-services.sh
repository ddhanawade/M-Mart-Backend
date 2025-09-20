#!/bin/bash

# Mahabaleshwer Mart Microservices Startup Script
# This script starts all microservices in the correct order

echo "🚀 Starting Mahabaleshwer Mart Microservices..."

# Function to check if a service is healthy
check_health() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo "✅ $service_name is healthy!"
            return 0
        fi
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 10
        ((attempt++))
    done
    
    echo "❌ $service_name failed to start within expected time"
    return 1
}

# Start infrastructure services
echo "📦 Starting infrastructure services..."
docker-compose up -d mysql redis rabbitmq

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services..."
sleep 30

# Start Config Server
echo "⚙️ Starting Config Server..."
docker-compose up -d config-server
check_health "Config Server" 8888

# Start Service Discovery
echo "🔍 Starting Service Discovery..."
docker-compose up -d service-discovery
check_health "Service Discovery" 8761

# Start API Gateway
echo "🌐 Starting API Gateway..."
docker-compose up -d api-gateway
check_health "API Gateway" 8080

# Start business services
echo "🏪 Starting business services..."
docker-compose up -d user-service product-service cart-service order-service notification-service

# Check all services
echo "🔍 Checking all services..."
check_health "User Service" 8081
check_health "Product Service" 8082
check_health "Cart Service" 8083
check_health "Order Service" 8084
check_health "Notification Service" 8085

echo "🎉 All services are up and running!"
echo ""
echo "📋 Service URLs:"
echo "   API Gateway: http://localhost:8080"
echo "   Eureka Dashboard: http://localhost:8761"
echo "   Config Server: http://localhost:8888"
echo ""
echo "🔧 Individual Services:"
echo "   User Service: http://localhost:8081"
echo "   Product Service: http://localhost:8082"
echo "   Cart Service: http://localhost:8083"
echo "   Order Service: http://localhost:8084"
echo "   Notification Service: http://localhost:8085"
echo ""
echo "✨ Happy coding!"
