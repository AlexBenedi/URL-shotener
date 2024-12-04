package es.unizar.urlshortener.integration.config

import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.PublishSubscribeChannelSpec
import org.springframework.integration.dsl.DirectChannelSpec
import org.springframework.integration.dsl.*
import org.springframework.integration.dsl.integrationFlow
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.integration.channel.DirectChannel
import es.unizar.urlshortener.integration.api.PubSubMessageSender
import org.springframework.context.ApplicationContext
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.Message

@Configuration
@EnableIntegration // Inicia el sustena de gestion de flujos
@EnableScheduling // Permute activar triggers temporales.
open class IntegrationConfig {

    @Bean
    open fun pubSubChannel(): PublishSubscribeChannelSpec<*> = MessageChannels.publishSubscribe()

    @Bean
    open fun direct(): DirectChannelSpec = MessageChannels.direct()

    /* 
    @Bean
    open fun prueba(direct: DirectChannel): IntegrationFlow = integrationFlow(
        source = { direct }
    ) {
        handle { payload: Any, headers: Map<String, Any> -> println("Prueba received: $payload") }
    }
    */
    
    
    @Bean
    open fun routerFlow(direct: DirectChannel): IntegrationFlow = integrationFlow(direct) {

        route<Message<*>> { message ->
            val type = message.headers["type"] as? String
            when (type) {
                "branded" -> "brandedChannel"
                "qr" -> "qrChannel"
                else -> "defaultChannel"
            }
        }
    }

    
    @Bean
    open fun qrFlow(): IntegrationFlow = integrationFlow("qrChannel") {
        handle { payload: Any, headers: Map<String, Any> -> println("QR Consumer received: $payload") }
    }

    @Bean
    open fun brandedFlow(): IntegrationFlow = integrationFlow("brandedChannel") {
        handle { payload: Any, headers: Map<String, Any> -> println("Llega") }
    }

    @Bean
    open fun defaultFlow(): IntegrationFlow = integrationFlow("defaultChannel") {
        handle { payload: Any, headers: Map<String, Any> -> println("Default Consumer received: $payload") }
    }
    

    /*
    @Bean
    open fun brandedFlow(pubSubChannel: PublishSubscribeChannel): IntegrationFlow = integrationFlow(
        source = { pubSubChannel }
    ) {
        filter { message: Message<*> -> message != null }
        filter { message: Message<*> -> 
            val type = message.headers["type"] as? String
            type == "branded"
        }
        handle { payload: Any, headers: Map<String, Any> -> println("Branded Consumer received: $payload") }
    }
    */
    
}