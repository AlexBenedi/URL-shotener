@file:Suppress("WildcardImport")

package es.unizar.urlshortener.springbootkafkaexample.service

import com.google.gson.Gson
import es.unizar.urlshortener.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient
import es.unizar.urlshortener.gateway.NinjaProfanityFilter
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCase
import es.unizar.urlshortener.core.usecases.UpdateUrlSafetyUseCase
import es.unizar.urlshortener.core.usecases.UpdateUrlBrandedUseCase
import es.unizar.urlshortener.core.usecases.StoreQRUseCase
import es.unizar.urlshortener.websockets.MyWebSocketClient
import es.unizar.urlshortener.websockets.WebSocketMessage
import java.net.URI
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type





// quizá esta clase irá en core? o en otro paquete?
// no le acabo de ver sentido a tenerla separada es una clase con mucho acoplamiento
// con otras pero no logro ver donde habrá q meterla, tengo q darle una vuelta más
@Service
class KafkaConsumerService(
    private val updateUrlSafetyUseCase: UpdateUrlSafetyUseCase,
    private val updateUrlBrandedUseCase: UpdateUrlBrandedUseCase,
    private val storeQRUseCase: StoreQRUseCase,
    private val generateQRCodeUseCase: GenerateQRCodeUseCase,
) {
    @Autowired 
    lateinit var googleSafeBrowsingClient: GoogleSafeBrowsingClient

    @Autowired
    lateinit var ninjaProfanityFilter: NinjaProfanityFilter

    @Autowired
    lateinit var kafkaProducerService: KafkaProducerService

    var lastConsumedMessage: String? = null

    // generic message
    @KafkaListener(topics = ["my_topic"], groupId = "group_id")
    fun consume(message: String) {
        println("Message received: $message")
        lastConsumedMessage = message
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
        println("Safety check requested for: $url, $res")
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

    @KafkaListener(topics = ["branded"], groupId = "group_id")
    fun consumeBranded(message: String) {
        println("Serielized branded received: $message")
        //Check
        val valid = ninjaProfanityFilter.isNameValid(message)
        updateUrlBrandedUseCase.updateUrlBranded(message, valid)
    }

    @KafkaListener(topics = ["qr"], groupId = "group_id")
    fun consumeQr(url: String) {
        println("Serielized QR received: $url")
        val deserializedObject = Gson().fromJson(url, UrlForQr::class.java)
        println("Url for the Qr received in Kafka: ${deserializedObject.id}")
        // Generate the QR code
        val qrCode = generateQRCodeUseCase.generateQRCode("http://localhost:8080/"+deserializedObject.id).base64Image
        println("QR code generated: $qrCode")

        // Enviar mensaje al WebSocket del usuario
        // Enviar el mensaje al WebSocket del usuario
        println("User id kafka: ${deserializedObject.userId}")
        val serverUri = URI("ws://localhost:8080/ws-endpoint")
        val webSocketClient = MyWebSocketClient(serverUri)
        webSocketClient.connectBlocking()
        
        // Serialize the user ID and the QR code
        val content: Pair<String, String> = Pair(deserializedObject.id, qrCode)
        val serialization: Type = object : TypeToken<Pair<String, String>>() {}.type
        val serializedContent = Gson().toJson(content, serialization)

        val webSocketMessage = WebSocketMessage(deserializedObject.userId, serializedContent)

        println("User and QR code serialized: $webSocketMessage")
        webSocketClient.send(Gson().toJson(webSocketMessage))
        webSocketClient.close()
        // Store the QR code in the database
        storeQRUseCase.storeQR(deserializedObject.id, qrCode)
        println("QR code stored in the database")
        // Send the QR code to the client VIA WEB SOCKETS
    }
}
