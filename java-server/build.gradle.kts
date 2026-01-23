plugins {
    id("java")
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.3"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "de.pse.oys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Web & REST (JSON via Jackson)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Datenbank (PostgreSQL & JPA/Hibernate)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Security für Hash-basierte Passwörter
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JJWT Core
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    // JJWT Implementierung
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    // Jackson für Claims
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // H2 In-Memory Database (für Tests und Entwicklung des Java-Servers ohne PostgreSQL Docker-Container)
    runtimeOnly("com.h2database:h2")

    // Junit 5 Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring Boot Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")

}

tasks.test {
    useJUnitPlatform()
}