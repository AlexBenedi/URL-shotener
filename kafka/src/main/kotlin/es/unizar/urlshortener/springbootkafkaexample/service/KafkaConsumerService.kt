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
import org.springframework.beans.factory.annotation.Value


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

    @Value("\${server.ip}")
    private lateinit var serverIp: String

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

        val checkedRes = UrlSafetyChecked(deserializedObject.id, res)

        // serialize the response and send it to the kafka topic
        val checkedResJson = Gson().toJson(checkedRes)
        kafkaProducerService.sendMessage("safety-checked", checkedResJson)
    }

    /* this method will be modified when spring integration/camel is implemented */
    @KafkaListener(topics = ["safety-checked"], groupId = "group_id")
    fun consumeSafetyChecked(message: String) {
        // deserialize the message to obtain the safety check result object
        val deserializedObject = Gson().fromJson(message, UrlSafetyChecked::class.java)

        // send the safety check result to the client
        updateUrlSafetyUseCase.updateUrlSafety(deserializedObject.id, deserializedObject.information)
    }

    @KafkaListener(topics = ["branded"], groupId = "group_id")
    fun consumeBranded(message: String) {
        //Check
        val valid = ninjaProfanityFilter.isNameValid(message)

        val validPair = Pair(message, valid)
        val validPairJson = Gson().toJson(validPair)  

        kafkaProducerService.sendMessage("branded-checked", validPairJson)
    }

    @KafkaListener(topics = ["branded-checked"], groupId = "group_id")
    fun consumeBrandedChecked(message: String) {
        val pairType = object : TypeToken<Pair<String, Boolean>>() {}.type
        val deserializedObject: Pair<String, Boolean> = Gson().fromJson(message, pairType)
        updateUrlBrandedUseCase.updateUrlBranded(deserializedObject.first, deserializedObject.second)
    }

    @KafkaListener(topics = ["qr"], groupId = "group_id")
    fun consumeQr(url: String) {
        val deserializedObject = Gson().fromJson(url, UrlForQr::class.java)
        // Generate the QR code
        val url = "http://${serverIp}/${deserializedObject.id}"
        val qrCode = generateQRCodeUseCase.generateQRCode(url).base64Image

        // Enviar mensaje al WebSocket del usuario
        val serverUri = URI("ws://localhost:8080/ws-endpoint")
        val webSocketClient = MyWebSocketClient(serverUri)
        webSocketClient.connectBlocking()
        
        // Serialize the user ID and the QR code
        val content: Pair<String, String> = Pair(deserializedObject.id, qrCode)
        val serialization: Type = object : TypeToken<Pair<String, String>>() {}.type
        val serializedContent = Gson().toJson(content, serialization)

        val webSocketMessage = WebSocketMessage(deserializedObject.userId, serializedContent)

        webSocketClient.send(Gson().toJson(webSocketMessage))
        webSocketClient.close()
        // Store the QR code in the database
        storeQRUseCase.storeQR(deserializedObject.id, qrCode)
    }
}
