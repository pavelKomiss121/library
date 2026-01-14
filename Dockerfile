# Stage 1: Build stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build --no-daemon -x test

COPY src ./src
RUN gradle bootJar --no-daemon

# Stage 2: Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создание пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/build/libs/*.jar application.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]

