plugins {
    // Aplicar las convenciones comunes del proyecto
    id("urlshortener-common-conventions")
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")

    // Aplicar el plugin de Spring Boot sin que se aplique automáticamente
    alias(libs.plugins.spring.boot) apply false

    // Aplicar el plugin de gestión de dependencias de Spring
    alias(libs.plugins.spring.dependency.management)

    // Aplicar el plugin de Kotlin JPA (opcional, pero puede ser útil en este caso)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":gateway"))
    implementation(project(":core"))

    // Dependencias específicas de WebSockets
    implementation(libs.spring.boot.starter.websocket) // Starter para WebSockets
    implementation(libs.spring.boot.starter.web)       // Starter para Web

    // Dependencia para la API de Jakarta WebSocket
    implementation("jakarta.websocket:jakarta.websocket-api:2.1.0")

    runtimeOnly(libs.spring.boot.devtools)

    compileOnly(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
}

dependencyManagement {
    imports {
        // Importar el BOM de Spring Boot para la gestión de dependencias
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Asegurar que todas las dependencias de org.jetbrains.kotlin usen la versión 1.9.23
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Asegurar que se ejecuten las pruebas antes de generar el informe

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // Generar el informe después de ejecutar las pruebas
}

sonar {
    properties {
        property("sonar.projectKey", "fractallink_url-shortener-websockets")
        property("sonar.organization", "fractallink")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}