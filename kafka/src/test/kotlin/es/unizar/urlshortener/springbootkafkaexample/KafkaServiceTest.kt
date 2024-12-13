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
    
    /**
     * Tests the `sendMessage` function of the `KafkaProducerService`.
     *
     * This test verifies that a message is correctly sent to the specified Kafka topic.
     *
     * Steps:
     * 1. Arrange: Set up the topic and message to be sent.
     * 2. Act: Call the `sendMessage` function with the topic and message.
     * 3. Capture the arguments passed to the `send` function of the `kafkaTemplate`.
     * 4. Verify: Ensure that the message was sent to the correct topic with the correct content.
     *
     * @throws Exception if the test fails
     */
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
 
    /**
     * Tests the `consume` function of the `KafkaConsumerService`.
     *
     * This test verifies that a message is correctly consumed from the specified Kafka topic.
     *
     * Steps:
     * 1. Arrange: Set up the topic and message to be consumed. Initialize `lastConsumedMessage` to null.
     * 2. Mock: Configure the `kafkaTemplate` to call the `consume` method of `kafkaConsumerService` when a message is sent.
     * 3. Act: Send the message to the Kafka topic using `kafkaTemplate`.
     * 4. Assert: Verify that the `lastConsumedMessage` in `kafkaConsumerService` matches the sent message.
     *
     * @throws Exception if the test fails
     */
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

    /**
     * Tests the safety checking process involving Kafka messaging.
     *
     * This test verifies that the safety checking process works correctly by sending a message to the `check-safety` topic,
     * processing it, and then sending the result to the `safety-checked` topic.
     *
     * Steps:
     * 1. Arrange: Set up the topics, messages, and mock responses.
     * 2. Mock: Configure the `googleSafeBrowsingClient` to return a predefined response.
     * 3. Mock: Configure the `kafkaTemplate` to call `consumeSafetyCheck` and `consumeSafetyChecked` methods of `kafkaConsumerService`.
     * 4. Act: Send the initial message to the `check-safety` topic.
     * 5. Assert: Verify that the messages were processed and sent to the correct topics, and that the `googleSafeBrowsingClient` was called.
     *
     * @throws Exception if the test fails
     */
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


    /**
     * Tests the branded link creation process involving Kafka messaging.
     *
     * This test verifies that the branded link creation process works correctly by sending a message to the `branded` topic,
     * processing it, and ensuring that the profanity filter is called.
     *
     * Steps:
     * 1. Arrange: Set up the topic, message, and mock responses.
     * 2. Mock: Configure the `ninjaProfanityFilter` to return a predefined response.
     * 3. Mock: Configure the `kafkaTemplate` to call the `consumeBranded` method of `kafkaConsumerService` when a message is sent.
     * 4. Act: Send the initial message to the `branded` topic.
     * 5. Assert: Verify that the message was processed, the `ninjaProfanityFilter` was called, and the Kafka consumer was invoked.
     *
     * @throws Exception if the test fails
     */
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

    /**
     * Tests the QR creation process involving Kafka messaging and WebSocket communication.
     *
     * This test verifies that the QR creation process works correctly by sending a message to the `qr` topic,
     * processing it, generating a QR code, storing it, and sending a WebSocket message.
     *
     * Steps:
     * 1. Arrange: Set up the topic, message, and mock responses.
     * 2. Mock: Configure the `generateQRCodeUseCase` to return a predefined QR code.
     * 3. Mock: Configure the `MyWebSocketClient` to simulate WebSocket behavior.
     * 4. Mock: Configure the `kafkaTemplate` to call the `consumeQr` method of `kafkaConsumerService` when a message is sent.
     * 5. Act: Send the initial message to the `qr` topic.
     * 6. Assert: Verify that the message was processed, the QR code was generated and stored, and the WebSocket client was used correctly.
     *
     * @throws Exception if the test fails
     */
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
