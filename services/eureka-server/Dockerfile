# Use OpenJDK as base image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/eureka-server-1.0.0.jar eureka-server.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
