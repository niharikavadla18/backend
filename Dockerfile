
# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make Maven Wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dserver.port=${PORT:8081}", "-jar", "app.jar"]
