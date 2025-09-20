# CORS Configuration for Mahabaleshwer Mart Backend Services

This document outlines the CORS (Cross-Origin Resource Sharing) configuration needed for the backend services to work with the Angular frontend.

## Overview

The Angular frontend runs on `http://localhost:4200` (development) and needs to communicate with backend services running on different ports. CORS configuration is required to allow these cross-origin requests.

## Service Ports

- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082  
- **Cart Service**: http://localhost:8083
- **Order Service**: http://localhost:8084
- **Notification Service**: http://localhost:8085

## Spring Boot CORS Configuration

### 1. Global CORS Configuration

Add this configuration class to each service:

```java
@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:4200",
                    "http://localhost:3000", 
                    "https://mahabaleshwermart.com",
                    "https://www.mahabaleshwermart.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 2. Application Properties Configuration

Add these properties to `application.yml` for each service:

```yaml
# CORS Configuration
app:
  security:
    allowed-origins:
      - "http://localhost:4200"
      - "http://localhost:3000"
      - "https://mahabaleshwermart.com"
      - "https://www.mahabaleshwermart.com"
    allowed-methods:
      - "GET"
      - "POST" 
      - "PUT"
      - "DELETE"
      - "OPTIONS"
      - "PATCH"
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

### 3. Security Configuration Updates

Update the SecurityConfig class in each service:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            // ... rest of security configuration
            ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:3000",
            "https://mahabaleshwermart.com",
            "https://*.mahabaleshwermart.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 4. Controller Level CORS (Alternative)

If global configuration doesn't work, add CORS at controller level:

```java
@RestController
@RequestMapping("/api/products")
@CrossOrigin(
    origins = {"http://localhost:4200", "https://mahabaleshwermart.com"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ProductController {
    // Controller methods
}
```

## Environment-Specific Configuration

### Development Environment

```yaml
app:
  security:
    allowed-origins:
      - "http://localhost:4200"
      - "http://localhost:3000"
```

### Production Environment

```yaml
app:
  security:
    allowed-origins:
      - "https://mahabaleshwermart.com"
      - "https://www.mahabaleshwermart.com"
      - "https://admin.mahabaleshwermart.com"
```

## Gateway Configuration (If using API Gateway)

If using Spring Cloud Gateway, add this configuration:

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:4200"
              - "https://mahabaleshwermart.com"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
              - PATCH
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
```

## Testing CORS Configuration

### 1. Browser Developer Tools

Open browser developer tools and check the Network tab for:
- OPTIONS preflight requests
- Response headers: `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`, etc.

### 2. Manual Testing

Use tools like Postman or curl to test CORS:

```bash
# Test OPTIONS request
curl -X OPTIONS \
  http://localhost:8081/api/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"

# Test actual request
curl -X POST \
  http://localhost:8081/api/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password"}'
```

## Common CORS Issues and Solutions

### Issue 1: CORS policy blocks request

**Solution**: Ensure the frontend origin is added to allowed origins list.

### Issue 2: Credentials not allowed

**Solution**: Set `allowCredentials(true)` in CORS configuration.

### Issue 3: Custom headers blocked

**Solution**: Add custom headers to `allowedHeaders` or use `"*"`.

### Issue 4: Preflight OPTIONS request fails

**Solution**: Ensure OPTIONS method is allowed and proper headers are set.

## Security Considerations

1. **Don't use wildcard (*) for origins in production**
2. **Specify exact origins for production**
3. **Limit allowed methods to what's actually needed**
4. **Be careful with `allowCredentials: true`**
5. **Set appropriate `maxAge` for preflight caching**

## Implementation Checklist

- [ ] Add CORS configuration to User Service (8081)
- [ ] Add CORS configuration to Product Service (8082)
- [ ] Add CORS configuration to Cart Service (8083)
- [ ] Add CORS configuration to Order Service (8084)
- [ ] Add CORS configuration to Notification Service (8085)
- [ ] Test all endpoints from frontend
- [ ] Verify preflight OPTIONS requests work
- [ ] Check authentication headers pass through
- [ ] Test in different browsers
- [ ] Verify production configuration

## Additional Notes

1. CORS is a browser security feature - server-to-server calls don't need CORS
2. Mobile apps don't typically need CORS configuration
3. Always test CORS configuration thoroughly before deploying to production
4. Consider using environment variables for allowed origins 