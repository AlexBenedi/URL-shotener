plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Spring Boot plugin without automatically applying it
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":gateway"))
    implementation(project(":core"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.kafka)
    implementation(libs.gson)

    runtimeOnly(libs.spring.boot.devtools)

    compileOnly(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.kafka.test)
}

dependencyManagement {
    imports {
        // Import the Spring Boot BOM (Bill of Materials) for dependency management
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Ensure that all dependencies from the org.jetbrains.kotlin group use version 1.9.23
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
    property("sonar.projectKey", "fractallink_url-shortener-kafka")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}
