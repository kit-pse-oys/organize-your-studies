# Die DockerFile zum Erstellen eines Docker-Images für das Modul "java-server".

# Build-Stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle Wrapper und Build-Dateien kopieren
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Zeilenumbrüche konvertieren und Gradle Wrapper ausführbar machen
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Quellcode kopieren und das Projekt bauen (ohne Test)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Runtime-Stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]