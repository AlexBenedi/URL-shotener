package es.unizar.urlshortener.springbootkafkaexample.service

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient
import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.UrlSafetyPetition
import es.unizar.urlshortener.core.UrlSafetyChecked
import es.unizar.urlshortener.core.usecases.UpdateUrlSafetyUseCase

// quizá esta clase irá en core? o en otro paquete?
// no le acabo de ver sentido a tenerla separada es una clase con mucho acoplamiento
// con otras pero no logro ver donde habrá q meterla, tengo q darle una vuelta más
@Service
class KafkaConsumerService(
    private val updateUrlSafetyUseCase: UpdateUrlSafetyUseCase
) {
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
        // deserialize the message to obtain the petition object
        val deserializedObject = Gson().fromJson(url, UrlSafetyPetition::class.java) 
        val res = googleSafeBrowsingClient.isUrlSafe(deserializedObject.url)
        println("Safety check requested for: $url")
        val checkedRes = UrlSafetyChecked(deserializedObject.id, res)
        println("Checked res: $checkedRes")
        // serialize the response and send it to the kafka topic
        val checkedResJson = Gson().toJson(checkedRes)
        kafkaProducerService.sendMessage("safety-checked", checkedResJson)
    }

    /* this method will be modified when spring integration/camel is implemented */
    @KafkaListener(topics = ["safety-checked"], groupId = "group_id")
    fun consumeSafetyChecked(message: String) {
        // habrá que ver como desacoplar esto....
        println("Serielized safety check result received: $message")
        // deserialize the message to obtain the safety check result object
        val deserializedObject = Gson().fromJson(message, UrlSafetyChecked::class.java)
        println("Safety check result received: $deserializedObject")
        // THIS WILL BE DONE USING WEBSOCKETS INSTEAD OF JUST CALLING THE FUNCTION DIRECTLY
        // send the safety check result to the client
        updateUrlSafetyUseCase.updateUrlSafety(deserializedObject.id, deserializedObject.information)
    }
}
