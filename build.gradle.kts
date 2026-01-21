plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "srangeldev"
version = "0.0.1-SNAPSHOT"
description = "MediaDaw"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")


    //Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Pebble Template Engine
    implementation("io.pebbletemplates:pebble-spring-boot-starter:3.2.4")
    implementation("io.pebbletemplates:pebble:3.2.4")

    // BBDD
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // Spring Boot DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // PDF Generation
    // implementation("com.itextpdf:itextpdf:5.5.13.4")
    // implementation("com.itextpdf:html2pdf:5.0.5")


    // Stripe
    // implementation("com.stripe:stripe-java:31.1.0")

    //Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.pebbletemplates:pebble-spring-boot-starter:4.1.0")
    implementation("io.pebbletemplates:pebble:4.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
