package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.springbootkafkaexample.service.KafkaProducerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaController(private val kafkaProducerService: KafkaProducerService) {

    @GetMapping("/send")
    fun sendMessage(@RequestParam message: String): String {
        kafkaProducerService.sendMessage("my_topic", message)
        return "Message sent successfully"
    }

    @PostMapping("/check-safety")
    fun checkSafety(@RequestParam url: String): String {
        kafkaProducerService.sendMessage("check-safety", url)
        return "Safety check requested"
    }
}
