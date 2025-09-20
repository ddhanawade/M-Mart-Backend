# Simple Dockerfile for Config Server using pre-built JAR
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -S configserver && adduser -S configserver -G configserver

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY config-server/target/config-server-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R configserver:configserver /app

# Switch to non-root user
USER configserver

# Expose port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
