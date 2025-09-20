# Simple Dockerfile for Service Discovery using pre-built JAR
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S eureka && adduser -S eureka -G eureka

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY service-discovery/target/service-discovery-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R eureka:eureka /app

# Switch to non-root user
USER eureka

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
