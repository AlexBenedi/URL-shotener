plugins {
    id("org.sonarqube") version "6.0.1.5171"
    id("org.jetbrains.dokka") version "1.9.0"
}

repositories {
    mavenCentral()
}

sonar {
  properties {
    property("sonar.projectKey", "fractallink_url-shortener-repositories")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    tasks.dokkaHtml {
      outputDirectory.set(file("$rootDir/docs/dokka/dokka-${project.name}"))

      dokkaSourceSets {
          configureEach {
              skipEmptyPackages.set(true)
          }
      }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.13.5")
        }
    }
}