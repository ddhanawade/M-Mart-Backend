# 🛒 M-Mart Backend - Complete End-to-End Flow Documentation

## 📋 Table of Contents
1. [Low Level Design (LLD)](#low-level-design-lld)
2. [End-to-End Flow Documentation](#end-to-end-flow-documentation)
3. [Microservices Information](#microservices-information)
4. [Architecture Diagrams & Explanations](#architecture-diagrams--explanations)
5. [Additional Technical Details](#additional-technical-details)

---

## 🏗️ Low Level Design (LLD)

### System Overview
M-Mart Backend is a comprehensive microservices-based e-commerce platform built with Java 21, Spring Boot 3.2+, and modern cloud-native technologies. The system follows Domain-Driven Design (DDD) principles with clear service boundaries.

### Core Architecture Patterns
- **Microservices Architecture**: 8 independent services with dedicated databases
- **API Gateway Pattern**: Single entry point for all client requests
- **Service Discovery Pattern**: Netflix Eureka for service registration
- **Event-Driven Architecture**: Kafka for asynchronous communication
- **CQRS Pattern**: Separate read/write models for complex operations
- **Circuit Breaker Pattern**: Resilience4j for fault tolerance

### Technology Stack
```
├── Core Framework: Spring Boot 3.2.1, Spring Cloud 2023.0.0
├── Language: Java 21 (Latest LTS)
├── Database: MySQL 8.0 (Database per Service)
├── Cache: Redis 7+ (Distributed Caching)
├── Message Queue: Apache Kafka (Event Streaming)
├── Service Discovery: Netflix Eureka
├── API Gateway: Spring Cloud Gateway
├── Authentication: JWT with Spring Security 6
├── Documentation: OpenAPI 3 (Swagger)
├── Containerization: Docker & Docker Compose
└── Monitoring: Spring Boot Actuator + Prometheus
```

### Database Design Strategy
Each microservice owns its data with dedicated MySQL databases:
- **User Service**: `mahabaleshwer_mart_users`
- **Product Service**: `mahabaleshwer_mart_products`
- **Cart Service**: `mahabaleshwer_mart_carts`
- **Order Service**: `mahabaleshwer_mart_orders`

---

## 🔄 End-to-End Flow Documentation

### 1. User Registration & Authentication Flow
```
Client → API Gateway → User Service → MySQL → Response
```
**Detailed Steps:**
1. Client sends registration request to API Gateway (Port 8080)
2. Gateway routes to User Service (Port 8081)
3. User Service validates data and stores in MySQL
4. Email verification sent via Notification Service
5. JWT tokens generated for authenticated sessions

### 2. Product Browsing & Search Flow
```
Client → API Gateway → Product Service → MySQL/Redis → Response
```
**Detailed Steps:**
1. Client requests product catalog through API Gateway
2. Gateway routes to Product Service (Port 8082)
3. Product Service queries MySQL with caching via Redis
4. Search functionality with pagination and filtering
5. Product details with inventory status returned

### 3. Shopping Cart Management Flow
```
Client → API Gateway → Cart Service → MySQL/Redis → Response
```
**Detailed Steps:**
1. Add/Update/Remove cart items through API Gateway
2. Gateway routes to Cart Service (Port 8083)
3. Cart Service manages session-based and user-based carts
4. Real-time price calculations and inventory validation
5. Cart persistence across sessions

### 4. Order Processing Flow (Complete E2E)
```
Client → API Gateway → Order Service → [Cart Service, User Service, Payment Service] → Kafka → Notification Service
```
**Detailed Steps:**
1. **Order Initiation**: Client submits order through API Gateway
2. **Cart Validation**: Order Service calls Cart Service to validate items
3. **User Verification**: Order Service calls User Service for profile data
4. **Payment Processing**: Integration with Payment Service (Mock/Real gateway)
5. **Order Creation**: Order stored in MySQL with timeline tracking
6. **Event Publishing**: Order confirmation event sent to Kafka
7. **Notification Dispatch**: Notification Service processes events
8. **Multi-channel Alerts**: Email/SMS notifications sent to customer

### 5. Notification & Communication Flow
```
Kafka Events → Notification Service → [Email Gateway, SMS Gateway] → Customer
```
**Detailed Steps:**
1. Order Service publishes events to Kafka topics
2. Notification Service consumes events from multiple topics
3. Template-based email generation using Thymeleaf
4. SMS notifications via configurable gateways
5. Event acknowledgment and retry mechanisms

---

## 🏢 Microservices Information

### 1. Config Server (Port 8888)
**Purpose**: Centralized configuration management
**Technology**: Spring Cloud Config Server
**Key Features**:
- Git-based configuration repository
- Environment-specific configurations (dev, test, prod)
- Real-time configuration refresh
- Encrypted sensitive properties

### 2. Service Discovery (Port 8761)
**Purpose**: Service registration and discovery
**Technology**: Netflix Eureka Server
**Key Features**:
- Automatic service registration
- Health monitoring and failover
- Load balancing support
- Web dashboard for monitoring

### 3. API Gateway (Port 8080)
**Purpose**: Single entry point and request routing
**Technology**: Spring Cloud Gateway
**Key Features**:
- JWT authentication and authorization
- Request routing with load balancing
- Rate limiting and throttling
- CORS handling
- Circuit breaker integration

### 4. User Service (Port 8081)
**Purpose**: Authentication and user management
**Database**: `mahabaleshwer_mart_users`
**Key Features**:
- User registration and authentication
- JWT token management (access + refresh tokens)
- Profile and address management
- Email verification workflow
- Role-based access control

**Core Entities**:
```java
User: id, email, name, phone, password, verified, createdAt
Address: id, userId, type, street, city, state, pincode, default
UserRole: id, userId, role, permissions
```

### 5. Product Service (Port 8082)
**Purpose**: Product catalog and inventory management
**Database**: `mahabaleshwer_mart_products`
**Key Features**:
- Product catalog management
- Category hierarchy management
- Advanced search with filtering
- Inventory tracking and stock management
- Product reviews and ratings

**Core Entities**:
```java
Product: id, name, description, price, category, sku, stock, images
Category: id, name, description, parentId, active
ProductReview: id, productId, userId, rating, comment, createdAt
Inventory: id, productId, quantity, reserved, available
```

### 6. Cart Service (Port 8083)
**Purpose**: Shopping cart management
**Database**: `mahabaleshwer_mart_carts`
**Key Features**:
- Session-based and user-based carts
- Real-time price calculations
- Cart persistence and synchronization
- Inventory validation
- Cart transfer (guest to user)

**Core Entities**:
```java
CartItem: id, userId, sessionId, productId, quantity, price, addedAt
CartSession: id, sessionId, userId, createdAt, lastAccessed
```

### 7. Order Service (Port 8084)
**Purpose**: Order processing and management
**Database**: `mahabaleshwer_mart_orders`
**Key Features**:
- Complete order lifecycle management
- Payment gateway integration
- Order status tracking with timeline
- Invoice generation
- Kafka event publishing

**Core Entities**:
```java
Order: id, orderNumber, userId, status, totalAmount, createdAt
OrderItem: id, orderId, productId, quantity, price, totalPrice
OrderTimeline: id, orderId, status, description, timestamp
Payment: id, orderId, method, status, transactionId, amount
```

### 8. Notification Service (Port 8085)
**Purpose**: Multi-channel notification management
**Key Features**:
- Event-driven notification processing
- Template-based email notifications
- SMS notifications (configurable)
- Kafka consumer for order events
- Retry mechanisms and error handling

**Kafka Topics**:
- `order-confirmed`: Order confirmation notifications
- `order-status-updated`: Order status change notifications
- `order-cancelled`: Order cancellation notifications
- `payment-confirmed`: Payment confirmation notifications
- `order-delivered`: Delivery notifications

---

## 🏛️ Architecture Diagrams & Explanations

### High-Level System Architecture
```
                    ┌─────────────────┐
                    │   Client Apps   │
                    │ (Web/Mobile/API)│
                    └─────────┬───────┘
                              │
                    ┌─────────▼───────┐
                    │   API Gateway   │
                    │   (Port 8080)   │
                    │  Authentication │
                    │   Rate Limiting │
                    └─────────┬───────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
    ┌───────▼──────┐ ┌────────▼────────┐ ┌─────▼──────┐
    │ User Service │ │ Product Service │ │Cart Service│
    │ (Port 8081)  │ │  (Port 8082)   │ │(Port 8083) │
    └──────┬───────┘ └─────────────────┘ └─────┬──────┘
           │                                   │
    ┌──────▼──────┐                    ┌──────▼──────┐
    │   MySQL     │                    │   MySQL     │
    │   Users     │                    │   Carts     │
    └─────────────┘                    └─────────────┘
```

### Service Communication Patterns

#### Synchronous Communication (REST/HTTP)
```
API Gateway ←→ Business Services (OpenFeign Clients)
Order Service ←→ Cart Service (Cart Validation)
Order Service ←→ User Service (Profile Fetching)
Order Service ←→ Payment Service (Payment Processing)
```

#### Asynchronous Communication (Kafka Events)
```
Order Service → Kafka Topics → Notification Service
Events: order-confirmed, order-status-updated, payment-confirmed
```

### Data Flow Architecture
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│ API Gateway │───▶│  Services   │
└─────────────┘    └─────────────┘    └──────┬──────┘
                                              │
┌─────────────┐    ┌─────────────┐           │
│ Notification│◀───│    Kafka    │◀──────────┘
│  Service    │    │   Events    │
└─────────────┘    └─────────────┘
```

### Security Architecture
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ JWT Token
┌──────▼──────┐
│ API Gateway │ ◀─── JWT Validation
└──────┬──────┘
       │ User Context Propagation
┌──────▼──────┐
│  Services   │ ◀─── Header-based Authentication
└─────────────┘
```

---

## 🔧 Additional Technical Details

### Inter-Service Communication Patterns

#### 1. Feign Client Configuration
```java
@FeignClient(name = "cart-service", path = "/api/internal/cart")
public interface CartServiceClient {
    @GetMapping("/validate/{userId}")
    ApiResponse<CartSummaryDto> validateCart(@PathVariable String userId);
}
```

#### 2. Kafka Event Publishing
```java
@Service
public class NotificationService {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void sendOrderConfirmation(Order order) {
        OrderNotificationEvent event = OrderNotificationEvent.builder()
            .eventType("ORDER_CONFIRMED")
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .build();
        kafkaTemplate.send("order-confirmed", order.getOrderNumber(), event);
    }
}
```

### Error Handling & Resilience

#### Circuit Breaker Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      cart-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
```

#### Global Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
        BusinessException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

### Performance Optimizations

#### 1. Database Optimizations
- Connection pooling with HikariCP
- Database indexing on frequently queried fields
- Pagination for large datasets
- Query optimization with JPA criteria

#### 2. Caching Strategy
- Redis for session management
- Application-level caching for product catalog
- Cache-aside pattern implementation

#### 3. Async Processing
- Kafka for event-driven notifications
- CompletableFuture for non-blocking operations
- Thread pool configuration for async tasks

### Monitoring & Observability

#### Health Check Endpoints
```
GET /actuator/health - Service health status
GET /actuator/metrics - Application metrics
GET /actuator/info - Service information
```

#### Logging Strategy
- Structured logging with correlation IDs
- Request/response logging in API Gateway
- Error tracking with stack traces
- Performance metrics logging

### Deployment Configuration

#### Docker Compose Services
```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mahabaleshwer_mart
    ports:
      - "3306:3306"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092
    ports:
      - "9092:9092"
```

### Security Implementation

#### JWT Authentication Flow
1. User login → User Service validates credentials
2. JWT tokens generated (access + refresh)
3. API Gateway validates JWT on each request
4. User context propagated to downstream services

#### API Security Features
- CORS configuration for cross-origin requests
- Rate limiting to prevent abuse
- Input validation and sanitization
- SQL injection prevention with JPA
- XSS protection with proper encoding

---

## 📊 Performance Metrics & SLAs

### Service Level Agreements
- **API Response Time**: < 200ms for 95% of requests
- **System Availability**: 99.9% uptime
- **Order Processing**: < 5 seconds end-to-end
- **Notification Delivery**: < 30 seconds

### Scalability Considerations
- Horizontal scaling with load balancers
- Database read replicas for read-heavy operations
- Kafka partitioning for event processing
- CDN integration for static content

---

**Document Version**: 1.0  
**Last Updated**: 2025-09-25  
**Author**: M-Mart Development Team  
**Status**: Production Ready ✅
