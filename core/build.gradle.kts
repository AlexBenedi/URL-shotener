plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")
    id("org.sonarqube") version "6.0.1.5171"
}

dependencies {
    // Add Kotlin test library for unit testing
    testImplementation(libs.kotlin.test)

    // Add Mockito Kotlin library for mocking in tests
    testImplementation(libs.mockito.kotlin)

    // Add JUnit Jupiter library for writing and running tests
    testImplementation(libs.junit.jupiter)

    // Add JUnit Platform Launcher for launching tests
    testRuntimeOnly(libs.junit.platform.launcher)

    // Required for MatrixToImageWriter
    implementation(libs.zxing.core)
    implementation(libs.zxing.javase)

    // Add this for SLF4J API
    implementation(libs.slf4j.api)
}

sonar {
  properties {
    property("sonar.projectKey", "fractallink_url-shortener-core")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.sources", "src/main/kotlin")
    property("sonar.tests", "src/test/kotlin")
  }
}
