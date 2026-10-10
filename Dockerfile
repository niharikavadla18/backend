
# Stage 1: Build the Spring Boot application
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven Wrapper and project files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make Maven Wrapper executable
RUN chmod +x mvnw

# Download dependencies and build the application
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render provides the PORT environment variable
EXPOSE 8081

# Start Spring Boot
ENTRYPOINT ["java", "-Dserver.port=${PORT:8081}", "-jar", "app.jar"]
