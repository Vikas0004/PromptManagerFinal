# Start from a base image with Java runtime
FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="you@example.com"
LABEL description="API Gateway Microservice"

# Set working directory
WORKDIR /app

# Copy the jar file (assumes it's built using Maven as target/api-gateway.jar)
COPY target/api-gateway-1.0.0.jar api-gateway.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
