plugins {
    // Applies the common conventions plugin for the URL shortener project.
    id("urlshortener-common-conventions")
    // Applies the Kotlin Spring plugin using an alias from the version catalog.
    alias(libs.plugins.kotlin.spring)
    // Applies the Spring Boot plugin using an alias from the version catalog.
    alias(libs.plugins.spring.boot)
    // Applies the Spring Dependency Management plugin using an alias from the version catalog.
    alias(libs.plugins.spring.dependency.management)
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")
}

dependencies {
    // Adds the core project as an implementation dependency.
    implementation(project(":core"))
    // Adds the delivery project as an implementation dependency.
    implementation(project(":delivery"))
    // Adds the repositories project as an implementation dependency.
    implementation(project(":repositories"))
    // Adds the sockets project as an implementation dependency.
    implementation(project(":WebSockets"))

    // Required for MatrixToImageWriter
    implementation(libs.zxing.core)
    implementation(libs.zxing.javase)

    // Adds the Spring Boot starter as an implementation dependency.
    implementation(libs.spring.boot.starter)
    // OAuth2 Client for authentication using OAuth2 & OpenID Connect
    implementation(libs.spring.boot.starter.oauth2.client)
    // Adds Bootstrap as an implementation dependency.
    implementation(libs.bootstrap)
    // Adds jQuery as an implementation dependency.
    implementation(libs.jquery)

    // Adds HSQLDB as a runtime-only dependency.
    runtimeOnly(libs.hsqldb)
    // Adds Kotlin reflection library as a runtime-only dependency.
    runtimeOnly(libs.kotlin.reflect)

    // Adds Kotlin test library as a test implementation dependency.
    testImplementation(libs.kotlin.test)
    // Adds Mockito Kotlin library as a test implementation dependency.
    testImplementation(libs.mockito.kotlin)
    // Adds Spring Boot starter test library as a test implementation dependency.
    testImplementation(libs.spring.boot.starter.test)
    // Adds Spring Boot starter web library as a test implementation dependency.
    testImplementation(libs.spring.boot.starter.web)
    // Adds Spring Boot starter JDBC library as a test implementation dependency.
    testImplementation(libs.spring.boot.starter.jdbc)
    // Adds Apache HttpClient 5 as a test implementation dependency.
    testImplementation(libs.httpclient5)
    // Adds JUnit Jupiter as a test implementation dependency.
    testImplementation(libs.junit.jupiter)
    // Adds JUnit Platform launcher as a test runtime-only dependency.
    testRuntimeOnly(libs.junit.platform.launcher)
}

dependencyManagement {
    imports {
        // Imports the Spring Boot BOM (Bill of Materials) for dependency management.
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Ensures that all dependencies from the org.jetbrains.kotlin group use version 1.9.23.
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
    property("sonar.projectKey", "fractallink_url-shortener-app")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.sources", "src/main/kotlin")
    property("sonar.tests", "src/test/kotlin")
    property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.13.5")
        }
    }
}