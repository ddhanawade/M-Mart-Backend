# 🛒 Mahabaleshwer Mart - Microservices Backend

## 🏗️ Architecture Overview

A comprehensive microservices-based e-commerce backend built with **Java 21** and **Spring Boot 3.2+**.

### 🔧 Technology Stack

- **Java**: 21 (Latest LTS)
- **Spring Boot**: 3.2.1
- **Spring Cloud**: 2023.0.0
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7+
- **Message Queue**: RabbitMQ
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: JWT with Spring Security 6
- **Documentation**: OpenAPI 3 (Swagger)
- **Containerization**: Docker & Docker Compose

### 🏢 Microservices

| Service | Port | Description |
|---------|------|-------------|
| **Config Server** | 8888 | Centralized configuration management |
| **Service Discovery** | 8761 | Eureka server for service registration |
| **API Gateway** | 8080 | Single entry point with routing & security |
| **User Service** | 8081 | Authentication, user management, addresses |
| **Product Service** | 8082 | Product catalog, categories, search |
| **Cart Service** | 8083 | Shopping cart management |
| **Order Service** | 8084 | Order processing, payment, tracking |
| **Notification Service** | 8085 | Email/SMS notifications |

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### 🐳 Docker Setup (Recommended)

```bash
# Clone the repository
cd mahabaleshwer-mart-backend

# Start infrastructure services
docker-compose up -d postgres redis rabbitmq

# Start all microservices
docker-compose up -d

# Check services status
docker-compose ps
```

### 🛠️ Local Development

```bash
# Start infrastructure
docker-compose up -d postgres redis rabbitmq

# Start services in order
cd config-server && mvn spring-boot:run &
cd service-discovery && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
cd user-service && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd cart-service && mvn spring-boot:run &
cd order-service && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
```

## 📚 API Documentation

- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Product Service**: http://localhost:8082/swagger-ui.html
- **Cart Service**: http://localhost:8083/swagger-ui.html
- **Order Service**: http://localhost:8084/swagger-ui.html

## 🔒 Authentication

The system uses JWT-based authentication:

1. **Login**: `POST /api/auth/login`
2. **Register**: `POST /api/auth/register`
3. **Refresh Token**: `POST /api/auth/refresh`

Include JWT token in header: `Authorization: Bearer <token>`

## 🗄️ Database Design

### User Service
- `users` - User profiles
- `addresses` - User addresses
- `user_roles` - User roles and permissions

### Product Service
- `products` - Product catalog
- `categories` - Product categories
- `product_reviews` - Product reviews and ratings
- `inventory` - Stock management

### Cart Service
- `cart_items` - Shopping cart items
- `cart_sessions` - Session-based carts

### Order Service
- `orders` - Order information
- `order_items` - Order line items
- `order_timeline` - Order status tracking
- `payments` - Payment information

## 🔄 Inter-Service Communication

- **Synchronous**: REST APIs via Spring Cloud OpenFeign
- **Asynchronous**: RabbitMQ for event-driven communication
- **Service Discovery**: Netflix Eureka
- **Load Balancing**: Spring Cloud LoadBalancer

## 📊 Monitoring & Observability

- **Health Checks**: Spring Boot Actuator
- **Metrics**: Micrometer + Prometheus
- **Tracing**: Spring Cloud Sleuth + Zipkin
- **Logging**: Logback with JSON format

## 🧪 Testing

```bash
# Run all tests
mvn clean test

# Run integration tests
mvn clean verify

# Generate test reports
mvn clean test jacoco:report
```

## 🔧 Configuration

Configuration is managed centrally via Config Server:

- **Development**: `application-dev.yml`
- **Testing**: `application-test.yml`
- **Production**: `application-prod.yml`

## 🚢 Deployment

### Production Deployment

```bash
# Build all services
mvn clean package -DskipTests

# Build Docker images
docker-compose build

# Deploy to production
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/
```

## 🔐 Security Features

- **JWT Authentication** with refresh tokens
- **Role-based Access Control** (RBAC)
- **API Rate Limiting**
- **Request/Response Logging**
- **CORS Configuration**
- **SQL Injection Prevention**
- **XSS Protection**

## 📈 Performance Features

- **Database Connection Pooling**
- **Redis Caching** for frequently accessed data
- **Async Processing** for notifications
- **Database Indexing** optimization
- **Response Compression**
- **Pagination** for large datasets

## 🛠️ Development Guidelines

### Code Standards
- Use **Lombok** for boilerplate reduction
- Follow **MapStruct** for DTO mapping
- Implement **proper exception handling**
- Write **comprehensive tests**
- Use **meaningful commit messages**

### API Standards
- Follow **REST principles**
- Use **consistent response formats**
- Implement **proper HTTP status codes**
- Include **comprehensive documentation**

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- **Documentation**: Check this README and service-specific docs
- **Issues**: Create GitHub issues for bugs
- **Discussions**: Use GitHub discussions for questions

---

**Built with ❤️ for Mahabaleshwer Mart - Fresh from Farm to Your Table! 🌱** 