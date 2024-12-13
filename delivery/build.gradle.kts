plugins {
    // Apply the common conventions plugin for the project
    id("urlshortener-common-conventions")
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")

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

    implementation(project(":gateway"))

    implementation(project(":kafka"))

    implementation(project(":WebSockets"))


    // Include Spring Boot Starter Web as an implementation dependency
    implementation(libs.spring.boot.starter.web)

    // Include Spring Boot Starter HATEOAS as an implementation dependency
    implementation(libs.spring.boot.starter.hateoas)

    // OAuth2 Client to authenticate using OAuth2 y OpenID Connect
    implementation(libs.spring.boot.starter.oauth2.client)

    // Include Apache Commons Validator as an implementation dependency
    implementation(libs.commons.validator)

    // Include Google Guava as an implementation dependency
    implementation(libs.guava)

    implementation(libs.zxing.core)
    implementation(libs.zxing.javase) // Required for MatrixToImageWriter
    
    implementation(libs.gson)

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

jacoco{
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Ensure tests are run before generating the report

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // Generate the report after tests run
}

sonar {
  properties {
    property("sonar.projectKey", "fractallink_url-shortener-delivery")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.15.0")
        }
    }
}
