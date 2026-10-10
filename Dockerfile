# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Configure an alternative Maven Central mirror
RUN mkdir -p /root/.m2 && printf '%s\n' 
'&lt;settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"&gt;' 
'  &lt;mirrors&gt;' 
'    &lt;mirror&gt;' 
'      &lt;id&gt;google-maven-central&lt;/id&gt;' 
'      &lt;name&gt;Google Maven Central Mirror&lt;/name&gt;' 
'      &lt;url&gt;https://maven-central.storage-download.googleapis.com/maven2/&lt;/url&gt;' 
'      &lt;mirrorOf&gt;central&lt;/mirrorOf&gt;' 
'    &lt;/mirror&gt;' 
'  &lt;/mirrors&gt;' 
'&lt;/settings&gt;' > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src

RUN mvn -s /root/.m2/settings.xml -B -ntp clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar app.jar"]