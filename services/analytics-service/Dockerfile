FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /build/target/analytics-service-1.0.0.jar analytics-service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "analytics-service.jar"]