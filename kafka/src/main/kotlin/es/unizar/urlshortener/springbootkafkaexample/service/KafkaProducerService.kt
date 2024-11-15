package es.unizar.urlshortener.springbootkafkaexample.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(private val kafkaTemplate: KafkaTemplate<String, String>) {

    companion object {
        private const val TOPIC = "my_topic"
    }

    fun sendMessage(message: String) {
        kafkaTemplate.send(TOPIC, message)
        println("Message sent: $message")
    }
}
