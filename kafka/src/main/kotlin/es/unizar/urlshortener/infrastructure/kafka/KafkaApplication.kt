package es.unizar.urlshortener.infrastructure.kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<KafkaApplication>(*args)
}
