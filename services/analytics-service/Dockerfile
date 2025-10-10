# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Add the built jar file to the container
COPY target/analytics-service-1.0.0.jar analytics-service.jar

# Expose the port the app runs on
EXPOSE 8083

# Run the jar file
ENTRYPOINT ["java", "-jar", "analytics-service.jar"]
