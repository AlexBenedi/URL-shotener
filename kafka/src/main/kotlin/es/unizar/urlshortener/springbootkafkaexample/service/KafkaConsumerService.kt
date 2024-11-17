package es.unizar.urlshortener.springbootkafkaexample.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient

@Service
class KafkaConsumerService {
    @Autowired 
    lateinit var googleSafeBrowsingClient: GoogleSafeBrowsingClient

    @KafkaListener(topics = ["my_topic"], groupId = "group_id")
    fun consume(message: String) {
        println("Message received: $message")
    }

    @KafkaListener(topics = ["check-safety"], groupId = "group_id")
    fun consumeSafetyCheck(url: String) {
        val cleanedUrl = url.trim('"') // dont know why it is surrounded by quotes
        val res = googleSafeBrowsingClient.isUrlSafe(cleanedUrl)
        println("Safety check requested for: $cleanedUrl")
        println("Result: $res")
    }
}
