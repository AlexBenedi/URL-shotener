package es.unizar.urlshortener.springbootkafkaexample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["es.unizar.urlshortener.springbootkafkaexample"])
class SpringBootKafkaExampleApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<SpringBootKafkaExampleApplication>(*args)
}
