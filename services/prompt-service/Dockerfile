# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Add the built jar file to the container
COPY target/prompt-service-1.0.0.jar prompt-service.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the jar file
ENTRYPOINT ["java", "-jar", "prompt-service.jar"]
