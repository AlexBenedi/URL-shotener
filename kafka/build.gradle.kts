plugins {
    // Applies the common conventions plugin for the URL shortener project.
    id("urlshortener-common-conventions")
    // Applies the Kotlin Spring plugin using an alias from the version catalog.
    alias(libs.plugins.kotlin.spring)
    // Applies the Spring Boot plugin using an alias from the version catalog.
    alias(libs.plugins.spring.boot)
    // Applies the Spring Dependency Management plugin using an alias from the version catalog.
    alias(libs.plugins.spring.dependency.management)
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    compileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
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
