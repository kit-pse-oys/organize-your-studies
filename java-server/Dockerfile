# Stage 1: Bauen
FROM gradle:8-jdk17 AS build
WORKDIR /home/gradle/src

# 1. Kopiert die Konfiguration
COPY --chown=gradle:gradle build.gradle settings.gradle ./

# 2. Kopiert den Quellcode
COPY --chown=gradle:gradle src ./src

# 3. Baut das Projekt
# -x test überspringt die Tests beim Bauen, das geht schneller
RUN gradle build --no-daemon -x test

# Stage 2: Ausführen
FROM openjdk:17-slim
WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]