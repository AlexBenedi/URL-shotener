package es.unizar.urlshortener.springbootkafkaexample.controller

import es.unizar.urlshortener.springbootkafkaexample.service.KafkaProducerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaController(private val kafkaProducerService: KafkaProducerService) {

    @GetMapping("/send")
    fun sendMessage(@RequestParam message: String): String {
        kafkaProducerService.sendMessage(message)
        return "Message sent successfully"
    }

    @PostMapping("/check-safety")
    fun checkSafety(@RequestParam url: String): String {
        kafkaProducerService.sendMessage(url)
        return "Safety check requested"
    }
}
