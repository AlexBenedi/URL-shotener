package es.unizar.urlshortener.integration.api

import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

import org.springframework.integration.channel.PublishSubscribeChannel

/*
@Service
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
*/


@Service
class PubSubMessageSender(private val direct: MessageChannel) {

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
        direct.send(message)
        println("Mensaje enviado al canal: Payload=$payload, Headers=$headers")
    }
}
