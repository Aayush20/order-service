FROM ubuntu:latest
LABEL authors="Aayush"

ENTRYPOINT ["top", "-b"]

# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/order-service-*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]


