package es.unizar.urlshortener.springbootkafkaexample.service

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService {

    @KafkaListener(topics = ["my_topic"], groupId = "group_id")
    fun consume(message: String) {
        println("Message received: $message")
    }
}
