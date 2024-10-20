@file:Suppress("MatchingDeclarationName", "WildcardImport")

package es.unizar.urlshortener.infrastructure.kafka

import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.kafka.core.KafkaTemplate

class KafkaServiceTest {

    private val kafkaTemplate: KafkaTemplate<String, String>
                = mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val kafkaProducerService = KafkaProducerService(kafkaTemplate)

    @Test
    fun `test sending message to Kafka`() {
        // Arrange
        val topic = "test"
        val message = "Hello, Kafka!"

        // Act
        kafkaProducerService.sendMessage(topic, message)

        // Assert
        val argumentCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(kafkaTemplate).send(argumentCaptor.capture(), argumentCaptor.capture())

        // Verify that the message was sent to the correct topic
        println(argumentCaptor.allValues[0] + " " + argumentCaptor.allValues[1])
        assert(argumentCaptor.allValues[0] == topic)
        assert(argumentCaptor.allValues[1] == message)
    }
    /// TODO PROBAR RECIBIR MENSAJE DE KAFKA 
    /// TODO TESTS TE INTEGRACIÓN CON EL SERVIDOR REAL DE KAFKA QUE CORRE EN DOCKER
    /// TODO REALIZAR UN MODO MANUAL CON UN ENDPOINT /kakfa QUE ENVÍE UN MENSAJE A KAFKA Y OTRO QUE LO RECIBA
    /// NO CORREN PRISA
}
