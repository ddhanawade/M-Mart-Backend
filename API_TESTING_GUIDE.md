# 🚀 **Mahabaleshwer Mart Backend - Complete API Testing Guide**

## **📋 Overview**

This comprehensive guide provides step-by-step API testing instructions for all 5 microservices in the Mahabaleshwer Mart backend system. All services are now UP and healthy!

### **🌐 Service Endpoints:**
- **User Service**: `http://localhost:8081`
- **Product Service**: `http://localhost:8082`
- **Cart Service**: `http://localhost:8083`
- **Order Service**: `http://localhost:8084`
- **Notification Service**: `http://localhost:8085` (Message-driven, no REST endpoints)

---

## **🔐 1. USER SERVICE (Port 8081) - Authentication & User Management**

### **Health Check**
```bash
curl -X GET http://localhost:8081/api/auth/health
```

### **1.1 User Registration**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "phone": "9876543210"
  }'
```

### **1.2 User Login**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**📝 Note:** Save the `accessToken` from the login response for authenticated requests!

### **1.3 Get Current User Profile**
```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **1.4 Refresh Token**
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### **1.5 User Logout**
```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## **🛍️ 2. PRODUCT SERVICE (Port 8082) - Product Catalog**

### **Health Check**
```bash
curl -X GET http://localhost:8082/api/products/health
```

### **2.1 Get All Products (Paginated)**
```bash
curl -X GET "http://localhost:8082/api/products?page=0&size=10&sortBy=name&sortDirection=asc"
```

### **2.2 Get Product by ID**
```bash
curl -X GET http://localhost:8082/api/products/PRODUCT_ID
```

### **2.3 Get Product by SKU**
```bash
curl -X GET http://localhost:8082/api/products/sku/PRODUCT_SKU
```

### **2.4 Search Products with Filters**
```bash
curl -X GET "http://localhost:8082/api/products/search?query=apple&category=FRUITS&minPrice=10&maxPrice=100&inStock=true&organic=true&page=0&size=10"
```

### **2.5 Get Products by Category**
```bash
curl -X GET "http://localhost:8082/api/products/category/FRUITS?page=0&size=10"
```

**Available Categories:** `FRUITS`, `VEGETABLES`, `DAIRY`, `GRAINS`, `SPICES`, `BEVERAGES`, `SNACKS`, `PERSONAL_CARE`, `HOUSEHOLD`

### **2.6 Get Featured Products**
```bash
curl -X GET "http://localhost:8082/api/products/featured?page=0&size=10"
```

### **2.7 Get Organic Products**
```bash
curl -X GET "http://localhost:8082/api/products/organic?page=0&size=10"
```

### **2.8 Get Products on Sale**
```bash
curl -X GET "http://localhost:8082/api/products/sale?page=0&size=10"
```

### **2.9 Get Top-Rated Products**
```bash
curl -X GET "http://localhost:8082/api/products/top-rated?page=0&size=10"
```

### **2.10 Get Related Products**
```bash
curl -X GET "http://localhost:8082/api/products/PRODUCT_ID/related?page=0&size=8"
```

### **2.11 Get Low Stock Products**
```bash
curl -X GET http://localhost:8082/api/products/low-stock
```

### **2.12 Get Product Count by Category**
```bash
curl -X GET http://localhost:8082/api/products/category-counts
```

---

## **🛒 3. CART SERVICE (Port 8083) - Shopping Cart Management**

### **Health Check**
```bash
curl -X GET http://localhost:8083/api/cart/health
```

### **3.1 Get User Cart (Authenticated)**
```bash
curl -X GET http://localhost:8083/api/cart \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **3.2 Get Guest Cart (Session-based)**
```bash
curl -X GET http://localhost:8083/api/cart/guest \
  -H "Cookie: JSESSIONID=YOUR_SESSION_ID"
```

### **3.3 Add Item to Cart**
```bash
curl -X POST http://localhost:8083/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "productId": "PRODUCT_ID",
    "quantity": 2
  }'
```

### **3.4 Update Cart Item Quantity**
```bash
curl -X PUT http://localhost:8083/api/cart/items/CART_ITEM_ID/quantity \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5
  }'
```

### **3.5 Remove Item from Cart**
```bash
curl -X DELETE http://localhost:8083/api/cart/items/CART_ITEM_ID
```

### **3.6 Clear Entire Cart**
```bash
curl -X DELETE http://localhost:8083/api/cart/clear \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **3.7 Get Cart Item Count**
```bash
curl -X GET http://localhost:8083/api/cart/count \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **3.8 Transfer Guest Cart to User (On Login)**
```bash
curl -X POST http://localhost:8083/api/cart/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "sessionId": "GUEST_SESSION_ID",
    "userId": "USER_ID",
    "mergeWithExisting": true
  }'
```

### **3.9 Validate Cart**
```bash
curl -X POST http://localhost:8083/api/cart/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## **📦 4. ORDER SERVICE (Port 8084) - Order Processing**

### **Health Check**
```bash
curl -X GET http://localhost:8084/api/orders/health
```

### **4.1 Create Order from Cart**
```bash
curl -X POST http://localhost:8084/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "deliveryAddress": {
      "fullName": "John Doe",
      "phone": "9876543210",
      "addressLine1": "123 Main Street",
      "addressLine2": "Apartment 4B",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "landmark": "Near Central Mall"
    },
    "paymentMethod": "CARD",
    "specialInstructions": "Please handle with care"
  }'
```

### **4.2 Get Order by ID**
```bash
curl -X GET http://localhost:8084/api/orders/ORDER_ID \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **4.3 Get Order by Order Number**
```bash
curl -X GET http://localhost:8084/api/orders/number/ORDER_NUMBER
```

### **4.4 Get User Orders (Paginated)**
```bash
curl -X GET "http://localhost:8084/api/orders/my-orders?page=0&size=10&sortDirection=desc" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **4.5 Track Order by Order Number**
```bash
curl -X GET http://localhost:8084/api/orders/track/ORDER_NUMBER
```

### **4.6 Cancel Order**
```bash
curl -X POST http://localhost:8084/api/orders/ORDER_ID/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "reason": "Changed my mind"
  }'
```

### **4.7 Get Orders by Status (Admin)**
```bash
curl -X GET "http://localhost:8084/api/orders/status/PENDING?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Available Order Statuses:** `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `RETURNED`

### **4.8 Update Order Status (Admin)**
```bash
curl -X PUT http://localhost:8084/api/orders/ORDER_ID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "orderStatus": "CONFIRMED",
    "notes": "Order confirmed and being processed"
  }'
```

### **4.9 Search Orders (Admin)**
```bash
curl -X GET "http://localhost:8084/api/orders/search?q=john.doe@example.com&page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### **4.10 Get Order Statistics**
```bash
curl -X GET "http://localhost:8084/api/orders/statistics?days=30" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## **📧 5. NOTIFICATION SERVICE (Port 8085) - Message-Driven Service**

The notification service is **message-driven** and doesn't expose REST endpoints. It listens to RabbitMQ events and sends notifications automatically when:

- Orders are created, confirmed, or status updated
- Payments are processed
- Orders are delivered or cancelled

### **Health Check**
```bash
curl -X GET http://localhost:8085/actuator/health
```

**📝 Note:** Notifications are triggered automatically by other services via RabbitMQ messaging.

---

## **🧪 Step-by-Step Testing Workflow**

### **Phase 1: Authentication Setup**
1. **Register a new user** using User Service endpoint 1.1
2. **Login** using User Service endpoint 1.2
3. **Save the access token** for subsequent authenticated requests

### **Phase 2: Product Discovery**
1. **Get all products** using Product Service endpoint 2.1
2. **Search for specific products** using endpoint 2.4
3. **Get products by category** using endpoint 2.5
4. **Note down product IDs** for cart operations

### **Phase 3: Cart Management**
1. **Add products to cart** using Cart Service endpoint 3.3
2. **View cart contents** using endpoint 3.1
3. **Update quantities** using endpoint 3.4
4. **Validate cart** using endpoint 3.9

### **Phase 4: Order Processing**
1. **Create order from cart** using Order Service endpoint 4.1
2. **Track the order** using endpoint 4.5
3. **View order history** using endpoint 4.4
4. **Check order statistics** using endpoint 4.10

### **Phase 5: System Verification**
1. **Check all health endpoints** to ensure services are running
2. **Verify notifications** are being processed (check logs)
3. **Test error scenarios** (invalid data, unauthorized access)

---

## **🔧 Testing Tools Recommendations**

### **Command Line:**
- **cURL** - For quick API testing (examples provided above)
- **HTTPie** - More user-friendly HTTP client

### **GUI Tools:**
- **Postman** - Import the cURL commands as a collection
- **Insomnia** - Alternative REST client
- **Thunder Client** - VS Code extension

### **API Documentation:**
- **Swagger UI** - Available at each service's `/swagger-ui.html` endpoint
- **OpenAPI Spec** - Available at `/v3/api-docs` endpoint

---

## **📊 Expected Response Formats**

All APIs return responses in this format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* Response data */ },
  "timestamp": "2024-01-19T19:18:50+05:30"
}
```

### **Error Response Format:**
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-01-19T19:18:50+05:30"
}
```

---

## **🚨 Important Notes**

1. **Authentication Required:** Most endpoints require Bearer token authentication
2. **Rate Limiting:** Services may implement rate limiting for production
3. **Data Validation:** All request bodies are validated according to defined schemas
4. **CORS Enabled:** All origins allowed for development (configure for production)
5. **Health Checks:** All services expose health endpoints for monitoring

---

## **🎯 Quick Test Commands**

### **Test All Health Endpoints:**
```bash
echo "=== HEALTH CHECK ALL SERVICES ==="
curl -s http://localhost:8081/api/auth/health && echo
curl -s http://localhost:8082/api/products/health && echo  
curl -s http://localhost:8083/api/cart/health && echo
curl -s http://localhost:8084/api/orders/health && echo
curl -s http://localhost:8085/actuator/health && echo
```

### **Complete User Journey Test:**
```bash
# 1. Register user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123","phone":"9876543210"}'

# 2. Login user  
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# 3. Get products
curl -X GET "http://localhost:8082/api/products?page=0&size=5"

# 4. Add to cart (replace TOKEN and PRODUCT_ID)
curl -X POST http://localhost:8083/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"productId":"PRODUCT_ID","quantity":1}'
```

---

**🎉 Your Mahabaleshwer Mart backend is now ready for comprehensive testing! All services are UP and healthy!** 🚀
