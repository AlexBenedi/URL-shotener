@file:Suppress("MatchingDeclarationName", "WildcardImport", "UnusedPrivateProperty")

package es.unizar.urlshortener.springbootkafkaexample

import com.google.gson.Gson
import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.UrlSafetyPetition
import es.unizar.urlshortener.core.UrlSafetyChecked
import es.unizar.urlshortener.core.usecases.StoreQRUseCase
import es.unizar.urlshortener.core.QRCode
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCase
import es.unizar.urlshortener.core.usecases.UpdateUrlBrandedUseCase
import es.unizar.urlshortener.springbootkafkaexample.service.KafkaConsumerService
import es.unizar.urlshortener.springbootkafkaexample.service.KafkaProducerService
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient
import es.unizar.urlshortener.gateway.NinjaProfanityFilter
import es.unizar.urlshortener.core.usecases.UpdateUrlSafetyUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.MockedConstruction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import es.unizar.urlshortener.core.WebSocketsService
import es.unizar.urlshortener.websockets.MyWebSocketClient
import java.util.concurrent.TimeUnit
import java.net.URI

@SpringBootTest(classes=[KafkaProducerService::class, KafkaConsumerService::class])
class KafkaServiceTest {

    @MockBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockBean
    private lateinit var updateUrlSafetyUseCase: UpdateUrlSafetyUseCase

    @MockBean
    private lateinit var updateBrandedUseCase: UpdateUrlBrandedUseCase    

    @MockBean
    private lateinit var googleSafeBrowsingClient: GoogleSafeBrowsingClient

    @MockBean
    private lateinit var storeQRUseCase: StoreQRUseCase

    @MockBean
    private lateinit var generateQRCodeUseCase: GenerateQRCodeUseCase

    @MockBean
    private lateinit var ninjaProfanityFilter: NinjaProfanityFilter

    @Autowired
    private lateinit var kafkaProducerService: KafkaProducerService

    @Autowired
    private lateinit var kafkaConsumerService: KafkaConsumerService 

    @BeforeEach
    fun setUp() {
        reset(kafkaTemplate)
    }
    
    // Test the KafkaProducerService, which sends a message to Kafka
    @Test
    fun `test sending message to Kafka`() {
        // Arrange
        val topic = "test"
        val message = "Hello, Kafka!"

        // Act
        kafkaProducerService.sendMessage(topic, message)

        // Argument captor to capture the topic and message sent to Kafka
        val topicCaptor = ArgumentCaptor.forClass(String::class.java)
        val messageCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(kafkaTemplate).send(topicCaptor.capture(), messageCaptor.capture())

        // Verify that the message was sent to the correct topic
        assertEquals(topic, topicCaptor.value)
        assertEquals(message, messageCaptor.value)
    }
 
    // Test the KafkaConsumerService, which consumes a message from Kafka
    @Test
    fun `test consuming message from Kafka`() {
        // Arrange
        val topic = "test"
        val message = "Hello, Kafka!"
        kafkaConsumerService.lastConsumedMessage = null

        // Mock KafkaTemplate -> when a message is sent to Kafka, we call 'consume'
        doAnswer {
            kafkaConsumerService.consume(message)
            null
        }.`when`(kafkaTemplate).send(eq(topic), eq(message))

        // Act
        kafkaTemplate.send(topic, message)

        // Assert
        assertEquals(message, kafkaConsumerService.lastConsumedMessage)
    }

    // Test the KafkaConsumerService, which consumes a message from Kafka
    @Test
    fun `test safety checking process works`() {
        // Arrange
        val topicCheck = "check-safety"
        val topicChecked = "safety-checked"
        val message = UrlSafetyPetition("key", "www.unizar.es")
        val messageJson = Gson().toJson(message)
        val googleApiResponse = UrlSafetyResponse(true)
        val googleApiResponseJson = Gson().toJson(googleApiResponse)
        val checkedResponse = UrlSafetyChecked("key", googleApiResponse)
        val checkedResponseJson = Gson().toJson(checkedResponse)
        kafkaConsumerService.lastConsumedMessage = null
        var timesKafkaCheck = 0;
        var timesKafkaCheked = 0;

        `when`(googleSafeBrowsingClient.isUrlSafe(message.url)).thenReturn(googleApiResponse)

        // when a message is sent to check-safety Kafka, we call 'consumeSafetyCheck'
        doAnswer {
            kafkaConsumerService.consumeSafetyCheck(messageJson)
            timesKafkaCheck++
            kafkaTemplate.send(topicChecked, googleApiResponseJson)
            null
        }.`when`(kafkaTemplate).send(eq(topicCheck), eq(messageJson))

        doAnswer {
            kafkaConsumerService.consumeSafetyChecked(checkedResponseJson)
            timesKafkaCheked++
            null
        }.`when`(kafkaTemplate).send(eq(topicChecked), eq(googleApiResponseJson))

        // Act
        kafkaTemplate.send(topicCheck, messageJson)


        assertEquals(1, timesKafkaCheck)
        assertEquals(1, timesKafkaCheked)
        verify(googleSafeBrowsingClient, times(1)).isUrlSafe(message.url)
    }


    @Test
    fun `branded link creation process works`() {
        // Arrange
        val topic = "branded"
        val message = "exampleBrand"
        val profanityResponse = true
        var timesKafka = 0

        `when`(ninjaProfanityFilter.isNameValid(message)).thenReturn(profanityResponse)

        // when a message is sent to branded Kafka, we call 'consumeBranded'
        doAnswer {
            kafkaConsumerService.consumeBranded(message)
            timesKafka++
            null
        }.`when`(kafkaTemplate).send(eq(topic), eq(message))

        // Act
        kafkaTemplate.send(topic, message)

        // Assert
        assertEquals(1, timesKafka)
        verify(ninjaProfanityFilter, times(1)).isNameValid(message)
    }

    @Test
    fun `QR creation process works`() {
        // Arrange
        var timesKafka = 0;
        val topic = "qr"
        val message = """{"url":"http://www.example.com","id":"example","userId":"0:0:0:0:0:0:0:1"}"""
        val qrCode = "base64QRCode"
        val serverUri = URI("ws://localhost:8080/ws-endpoint")
        val webSocketMessage = """{"userId":"0:0:0:0:0:0:0:1","content":"[\"example\",\"base64QRCode\"]"}"""

        `when`(generateQRCodeUseCase.generateQRCode("http://localhost:8080/example"))
                                                    .thenReturn(QRCode(base64Image = qrCode,
                                                                       url = "http://localhost:8080/example",
                                                                       size = 250
                                                                       )
                                                                )
        val mockedConstruction: MockedConstruction<MyWebSocketClient> 
                = mockConstruction(MyWebSocketClient::class.java) 
                    { mock, context ->
                        `when`(mock.connectBlocking()).thenReturn(true)
                        doNothing().`when`(mock).send(any(String::class.java))
                        doNothing().`when`(mock).close()
                    }

        doAnswer {
            kafkaConsumerService.consumeQr(message)
            timesKafka++
            null
        }.`when`(kafkaTemplate).send(eq(topic), eq(message))

        // Act
        kafkaTemplate.send(topic, message)

        // Assert
        assertEquals(1, timesKafka)
        verify(generateQRCodeUseCase, times(1)).generateQRCode("http://localhost:8080/example")
        verify(storeQRUseCase, times(1)).storeQR("example", qrCode)
        verify(mockedConstruction.constructed().first(), times(1)).connectBlocking()
        verify(mockedConstruction.constructed().first(), times(1)).send(any(String::class.java))
        verify(mockedConstruction.constructed().first(), times(1)).close()
    }
}
