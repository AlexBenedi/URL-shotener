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

    // Include Spring Kafka as an implementation dependency
    implementation(libs.spring.kafka)

    // Include Spring Kafka Test as a test implementation dependency
    testImplementation(libs.spring.kafka.test)

    // Include Spring Boot Starter Test as a test implementation dependency
    testImplementation(libs.spring.boot.starter.test)
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