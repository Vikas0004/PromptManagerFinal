# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies first (Docker cache optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /build/target/prompt-service-1.0.0.jar prompt-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "prompt-service.jar"]