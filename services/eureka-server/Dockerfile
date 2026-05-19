FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /build/target/eureka-server-1.0.0.jar eureka-server.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "eureka-server.jar"]


# Use OpenJDK as base image
#FROM openjdk:17-jdk-slim

#WORKDIR /app

#COPY target/eureka-server-1.0.0.jar eureka-server.jar

#EXPOSE 8761

#ENTRYPOINT ["java", "-jar", "eureka-server.jar"]

