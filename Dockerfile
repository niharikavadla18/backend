```dockerfile
# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Configure an alternative Maven Central mirror
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOF'
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <mirrors>
    <mirror>
      <id>google-maven-central</id>
      <name>Google Maven Central Mirror</name>
      <url>https://maven-central.storage-download.googleapis.com/maven2/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -s /root/.m2/settings.xml -B -ntp clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar app.jar"]
```