package es.unizar.urlshortener.infrastructure.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

// Class which defines kafka producer service
@Service
class KafkaProducerService(private val kafkaTemplate: KafkaTemplate<String, String>) {

    // Sends a message to the specified topic
    fun sendMessage(topic: String, message: String) {
        kafkaTemplate.send(topic, message)
    }
}

// Class which defines kafka consumer service
@Service
class KafkaConsumerService {
    var lastReceivedMessage: String? = null

    // Listens to the messages sent to the specified topic and group
    @KafkaListener(topics = ["test"], groupId = "group_id")
    fun consume(message: String) {
        println("Consumed message: $message")
        lastReceivedMessage = message
    }
}
