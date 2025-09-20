# Simple Dockerfile for API Gateway using pre-built JAR
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S gateway && adduser -S gateway -G gateway

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY api-gateway/target/api-gateway-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R gateway:gateway /app

# Switch to non-root user
USER gateway

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
