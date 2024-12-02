plugins {
    id("org.sonarqube") version "6.0.1.5171"
}

sonar {
  properties {
    property("sonar.projectKey", "fractallink_url-shortener-repositories")
    property("sonar.organization", "fractallink")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}
