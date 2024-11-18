package es.unizar.urlshortener.springbootkafkaexample.service

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient
import es.unizar.urlshortener.core.UrlSafetyResponse

// quizá esta clase irá en core? o en otro paquete?
// no le acabo de ver sentido a tenerla separada es una clase con mucho acoplamiento
// con otras pero no logro ver donde habrá q meterla, tengo q darle una vuelta más
@Service
class KafkaConsumerService {
    @Autowired 
    lateinit var googleSafeBrowsingClient: GoogleSafeBrowsingClient

    @Autowired
    lateinit var kafkaProducerService: KafkaProducerService

    @KafkaListener(topics = ["my_topic"], groupId = "group_id")
    fun consume(message: String) {
        println("Message received: $message")
    }

    /* this method will be modified when spring integration/camel is implemented */
    @KafkaListener(topics = ["check-safety"], groupId = "group_id")
    fun consumeSafetyCheck(url: String) {
        // instead of doing this here, we should use a camel route to send the message to the gateway
        // and then the gateway will send the message to the google safe browsing api
        // and then  camel will send the response to the kafka topic / websockets 
        // something like: camel.send("direct:check-safety", url)
        val res = googleSafeBrowsingClient.isUrlSafe(url)
        println("Safety check requested for: $url")
        val jsonString = Gson().toJson(res)
        kafkaProducerService.sendMessage("safety-checked", jsonString)
    }

    /* this method will be modified when spring integration/camel is implemented */
    @KafkaListener(topics = ["safety-checked"], groupId = "group_id")
    fun consumeSafetyChecked(message: String) {
        // habrá que ver como desacoplar esto....
        println("Serielized safety check result received: $message")
        val deserializedObject = Gson().fromJson(message, UrlSafetyResponse::class.java)
        println("Safety check result received: $deserializedObject")
    }
}
