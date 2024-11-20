package es.unizar.urlshortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

/**
 * The marker that makes this project a Spring Boot application.
 */
@SpringBootApplication
@EnableWebSecurity
class Application

/**
 * The main entry point.
 * @param args The command line arguments.
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<Application>(*args)
}
