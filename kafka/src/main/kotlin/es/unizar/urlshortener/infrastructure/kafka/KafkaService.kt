package es.unizar.urlshortener.infrastructure.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch

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
    var latch = CountDownLatch(1)

    /*
     * Consumes a message from the specified topic and prints it.
     * As it is an example, it only prints the message and stores it in the lastReceivedMessage variable, 
     * and only listens to the 'test' topic.
     */
    @KafkaListener(topics = ["test"], groupId = "group_id")
    fun consume(message: String) {
        println("Consumed message: $message")
        lastReceivedMessage = message
        latch.countDown()
    }
}
