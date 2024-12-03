package es.unizar.urlshortener.integration.api

import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Component

@Component
class PubSubMessageSender(private val pubSubChannel: MessageChannel) {

    fun sendQrMessage(payload: Any) {
        sendMessage(payload, mapOf("type" to "qr"))
    }

    fun sendBrandedMessage(payload: Any) {
        sendMessage(payload, mapOf("type" to "branded"))
    }

    private fun sendMessage(payload: Any, headers: Map<String, Any> = emptyMap()) {
        val message = MessageBuilder.withPayload(payload)
            .copyHeaders(headers)
            .build()
        pubSubChannel.send(message)
        println("Mensaje enviado al canal: Payload=$payload, Headers=$headers")
    }
}
