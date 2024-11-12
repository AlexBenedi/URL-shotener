@file:Suppress("MatchingDeclarationName", "WildcardImport")

package es.unizar.urlshortener.infrastructure.kafka

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KafkaServiceTest {

    private val kafkaTemplate: KafkaTemplate<String, String>
                = mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val kafkaProducerService = KafkaProducerService(kafkaTemplate)

    private val kafkaConsumerService = KafkaConsumerService()

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
        kafkaConsumerService.lastReceivedMessage = null
        kafkaConsumerService.latch = CountDownLatch(1)

        // Mock KafkaTemplate -> when a message is sent to Kafka, we call 'consume'
        doAnswer {
            kafkaConsumerService.consume(message)
            null
        }.`when`(kafkaTemplate).send(eq(topic), eq(message))

        // Act
        kafkaTemplate.send(topic, message)
        kafkaConsumerService.latch.await(10, TimeUnit.SECONDS)

        // Assert
        assertEquals(message, kafkaConsumerService.lastReceivedMessage)
    }
    
    /*
     * Test the interaction between the KafkaProducerService and the KafkaConsumerService
     * The producer sends a message to Kafka, and the consumer consumes it. 
     * It is very similar to the previous test, but it uses both producer and consumer services.
     */ 
    @Test
    fun `test producer and consumer interaction`() {
        // Arrange
        val topic = "test"
        val message = "Hello, Kafka!"
        kafkaConsumerService.lastReceivedMessage = null
        kafkaConsumerService.latch = CountDownLatch(1)

        // Mock KafkaTemplate -> when a message is sent to Kafka, we call 'consume'
        doAnswer {
            kafkaConsumerService.consume(message)
            null
        }.`when`(kafkaTemplate).send(eq(topic), eq(message))

        // Act
        kafkaProducerService.sendMessage(topic, message)
        kafkaConsumerService.latch.await(10, TimeUnit.SECONDS)

        // Assert
        assertEquals(message, kafkaConsumerService.lastReceivedMessage)
    }

    /// TODO TESTS TE INTEGRACIÓN CON EL SERVIDOR REAL DE KAFKA QUE CORRE EN DOCKER
    /// TODO REALIZAR UN MODO MANUAL CON UN ENDPOINT /kakfa QUE ENVÍE UN MENSAJE A KAFKA Y OTRO QUE LO RECIBA
    /// NO CORREN PRISA
}
