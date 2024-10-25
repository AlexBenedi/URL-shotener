plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")
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

    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1") // Required for MatrixToImageWriter

    implementation("org.slf4j:slf4j-api:2.0.7") // Add this line for SLF4J API
    //implementation("org.slf4j:slf4j-simple:2.0.7") // Add this for a simple logger implementation
}
