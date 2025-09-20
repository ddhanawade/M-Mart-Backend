# Multi-stage build for Order Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy parent POM and all modules for multi-module build
COPY pom.xml .
COPY common/ ./common/
COPY user-service/ ./user-service/
COPY product-service/ ./product-service/
COPY cart-service/ ./cart-service/
COPY order-service/ ./order-service/
COPY notification-service/ ./notification-service/

# Build the application
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests -pl order-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from builder stage
COPY --from=builder /app/order-service/target/order-service-*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# JVM optimization for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
