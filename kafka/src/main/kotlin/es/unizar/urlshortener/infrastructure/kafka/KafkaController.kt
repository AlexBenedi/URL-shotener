package es.unizar.urlshortener.infrastructure.kafka

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaController(private val kafkaProducerService: KafkaProducerService) {

    @GetMapping("/send")
    fun sendMessage(@RequestParam message: String): String {
        kafkaProducerService.sendMessage("test", message)
        return "Message sent successfully"
    }
}
