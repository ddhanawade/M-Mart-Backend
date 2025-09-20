# Mahabaleshwer Mart - Microservices Architecture

## Overview

This document provides a comprehensive overview of the Mahabaleshwer Mart microservices architecture, built using Spring Boot 3.2.1, Spring Cloud 2023.0.0, and Java 21.

## Architecture Components

### Core Infrastructure Services

#### 1. Config Server (Port: 8888)
- **Purpose**: Centralized configuration management for all microservices
- **Technology**: Spring Cloud Config Server
- **Features**:
  - Git-based configuration repository
  - Environment-specific configurations
  - Real-time configuration updates
  - Security for sensitive configurations

#### 2. Service Discovery (Port: 8761)
- **Purpose**: Service registration and discovery using Netflix Eureka
- **Technology**: Spring Cloud Netflix Eureka Server
- **Features**:
  - Service registration and health monitoring
  - Load balancing support
  - Failover and resilience
  - Dashboard for monitoring registered services

#### 3. API Gateway (Port: 8080)
- **Purpose**: Single entry point for all client requests
- **Technology**: Spring Cloud Gateway
- **Features**:
  - Request routing and load balancing
  - JWT authentication and authorization
  - Rate limiting and throttling
  - Circuit breaker pattern
  - CORS handling
  - Request/response logging

### Business Services

#### 1. User Service (Port: 8081)
- **Purpose**: Authentication, user management, and address management
- **Database**: `mahabaleshwer_mart_users`
- **Features**:
  - User registration and authentication
  - JWT token management
  - Profile management
  - Address management
  - Email verification

#### 2. Product Service (Port: 8082)
- **Purpose**: Product catalog, categories, search, and inventory management
- **Database**: `mahabaleshwer_mart_products`
- **Features**:
  - Product catalog management
  - Category management
  - Search functionality with Elasticsearch
  - Inventory tracking
  - Product reviews and ratings

#### 3. Cart Service (Port: 8083)
- **Purpose**: Shopping cart management
- **Database**: `mahabaleshwer_mart_carts`
- **Features**:
  - Cart item management
  - Session-based and user-based carts
  - Cart persistence
  - Price calculations

#### 4. Order Service (Port: 8084)
- **Purpose**: Order processing, payment integration, and order tracking
- **Database**: `mahabaleshwer_mart_orders`
- **Features**:
  - Order creation and processing
  - Payment gateway integration
  - Order status tracking
  - Invoice generation
  - State machine for order workflow

#### 5. Notification Service (Port: 8085)
- **Purpose**: Email, SMS, and push notification management
- **Features**:
  - Email notifications
  - SMS notifications (configurable)
  - Push notifications (configurable)
  - Template-based messaging
  - Event-driven notifications

## Technology Stack

### Core Technologies
- **Java**: 21
- **Spring Boot**: 3.2.1
- **Spring Cloud**: 2023.0.0
- **Maven**: 3.9.x

### Infrastructure
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Message Queue**: RabbitMQ 3
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Configuration**: Spring Cloud Config

### Resilience & Monitoring
- **Circuit Breaker**: Resilience4j
- **Load Balancer**: Spring Cloud LoadBalancer
- **Monitoring**: Spring Boot Actuator + Prometheus
- **Documentation**: OpenAPI 3 (Swagger)

## Service Communication

### Synchronous Communication
- **API Gateway** → **Business Services**: HTTP/REST via load balancer
- **Inter-service calls**: OpenFeign clients with circuit breakers

### Asynchronous Communication
- **Event-driven**: RabbitMQ for order processing and notifications
- **Message patterns**: Publish/Subscribe, Request/Reply

## Security

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication
- **API Gateway**: Centralized authentication and authorization
- **Service-to-Service**: Headers propagation for user context

### Security Features
- **HTTPS**: SSL/TLS encryption
- **CORS**: Cross-origin resource sharing configuration
- **Rate Limiting**: Request throttling
- **Input Validation**: Bean validation across all services

## Data Management

### Database Strategy
- **Database per Service**: Each microservice has its own database
- **MySQL Databases**:
  - `mahabaleshwer_mart_users`
  - `mahabaleshwer_mart_products`
  - `mahabaleshwer_mart_carts`
  - `mahabaleshwer_mart_orders`

### Caching Strategy
- **Redis**: Distributed caching for session management and frequently accessed data
- **Service-level caching**: Application-level caching for performance optimization

## Deployment

### Docker Containerization
- Each service is containerized with multi-stage Docker builds
- Optimized images with non-root users for security
- Health checks for container orchestration

### Docker Compose
- Complete environment setup with single command
- Service dependencies and startup order
- Environment-specific configurations

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

### Quick Start
```bash
# Clone the repository
git clone https://github.com/ddhanawade/M-Mart-Backend.git
cd M-Mart-Backend

# Start all services
./start-services.sh

# Or manually with Docker Compose
docker-compose up -d
```

### Service URLs
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Individual Services**: Ports 8081-8085

## Development

### Local Development Setup
1. Start infrastructure services: `docker-compose up -d mysql redis rabbitmq`
2. Start Config Server and Service Discovery
3. Start individual services in your IDE
4. Access services through API Gateway at http://localhost:8080

### Configuration Management
- Centralized configurations in Config Server
- Environment-specific properties
- Sensitive data through environment variables

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator endpoints
- Custom health indicators
- Docker health checks

### Metrics
- Prometheus metrics export
- Circuit breaker metrics
- Custom business metrics

### Logging
- Structured logging with correlation IDs
- Centralized log aggregation ready
- Request/response tracing in API Gateway

## Best Practices Implemented

### Microservices Patterns
- **API Gateway Pattern**: Single entry point
- **Service Registry Pattern**: Service discovery
- **Circuit Breaker Pattern**: Fault tolerance
- **Configuration Server Pattern**: Centralized config
- **Database per Service**: Data isolation

### Development Practices
- **Clean Architecture**: Separation of concerns
- **SOLID Principles**: Object-oriented design
- **DRY Principle**: Code reusability through common module
- **Test-Driven Development**: Comprehensive test coverage
- **Documentation**: OpenAPI specifications

### Security Best Practices
- **Principle of Least Privilege**: Minimal permissions
- **Defense in Depth**: Multiple security layers
- **Secure by Default**: Secure configurations
- **Input Validation**: Data sanitization
- **Audit Logging**: Security event tracking

## Troubleshooting

### Common Issues
1. **Service Discovery Issues**: Check Eureka server connectivity
2. **Configuration Issues**: Verify Config Server accessibility
3. **Database Connection**: Ensure MySQL is running and accessible
4. **Port Conflicts**: Check if ports 8080-8888 are available

### Debugging
- Check service logs: `docker-compose logs <service-name>`
- Verify service health: `curl http://localhost:<port>/actuator/health`
- Monitor Eureka dashboard: http://localhost:8761

## Future Enhancements

### Planned Features
- **Distributed Tracing**: Zipkin/Jaeger integration
- **Event Sourcing**: CQRS pattern implementation
- **API Versioning**: Backward compatibility
- **Kubernetes Deployment**: Container orchestration
- **Service Mesh**: Istio integration

### Scalability Considerations
- **Horizontal Scaling**: Load balancer configuration
- **Database Sharding**: Data partitioning strategies
- **Caching Strategies**: Multi-level caching
- **CDN Integration**: Static content delivery

## Contributing

### Development Workflow
1. Create feature branch from main
2. Implement changes with tests
3. Update documentation
4. Submit pull request
5. Code review and merge

### Code Standards
- Follow Spring Boot best practices
- Maintain test coverage above 80%
- Use conventional commit messages
- Update API documentation

---

**Author**: Mahabaleshwer Mart Development Team  
**Version**: 1.0.0  
**Last Updated**: 2025-01-20
