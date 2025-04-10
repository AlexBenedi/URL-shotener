plugins {
    // Apply the common conventions plugin for the project
    id("urlshortener-common-conventions")
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco") 

    // Apply the Spring Boot plugin but do not apply it immediately
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Include the core project as an implementation dependency
    implementation(project(":core"))
    
    configurations {
        all {
            exclude(group = "commons-logging", module = "commons-logging")
        }
    }
   
   // Include Spring Boot Starter Web for creating web applications
    implementation(libs.spring.boot.starter.web)

    // Include Google Safe Browsing API client
    implementation(libs.google.api.client )

    // Include Spring Boot Starter Test for testing
    testImplementation(libs.spring.boot.starter.test)

    // Include additional testing libraries if needed
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
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

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.15.0")
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
    property("sonar.projectKey", "fractallink_url-shortener-gateway")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}