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

    // H2 In-Memory Database (für Tests und Entwicklung des Java-Servers ohne PostgreSQL Docker-Container)
    runtimeOnly("com.h2database:h2")

    // Junit 5 Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}