plugins {
    // Apply the common conventions plugin for the project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Kotlin Spring plugin
    alias(libs.plugins.kotlin.spring)

    // Apply the Spring Boot plugin but do not apply it immediately
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Include the core project as an implementation dependency
    implementation(project(":core"))

    // Include Spring Boot Starter Web as an implementation dependency
    implementation(libs.spring.boot.starter.web)

    // Include Spring Boot Starter HATEOAS as an implementation dependency
    implementation(libs.spring.boot.starter.hateoas)

    // Include Apache Commons Validator as an implementation dependency
    implementation(libs.commons.validator)

    // Include Google Guava as an implementation dependency
    implementation(libs.guava)

    // Include Kotlin Test as a test implementation dependency
    testImplementation(libs.kotlin.test)

    // Include Mockito Kotlin as a test implementation dependency
    testImplementation(libs.mockito.kotlin)

    // Include JUnit Jupiter as a test implementation dependency
    testImplementation(libs.junit.jupiter)

    // Include JUnit Platform Launcher as a test runtime-only dependency
    testRuntimeOnly(libs.junit.platform.launcher)

    // Include Spring Boot Starter Test as a test implementation dependency
    testImplementation(libs.spring.boot.starter.test)

    // Spring Boot Web
    implementation ("org.springframework.boot:spring-boot-starter-web")

    // Spring Security Core (manejo de la seguridad)
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.security:spring-security-config")

    // OAuth2 Client para autenticación con OAuth2 y OpenID Connect
    implementation ("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Dependencias para pruebas (si necesitas realizar tests relacionados con seguridad)
    testImplementation ("org.springframework.boot:spring-boot-starter-test")
    testImplementation ("org.springframework.security:spring-security-test")
}

dependencyManagement {
    imports {
        // Import the Spring Boot BOM (Bill of Materials) for dependency management
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Force the use of Kotlin version 1.9.23 for all dependencies in the detekt configuration
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}
