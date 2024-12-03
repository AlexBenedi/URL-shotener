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
import org.springframework.integration.dsl.integrationFlow
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

import es.unizar.urlshortener.integration.api.PubSubMessageSender

@Configuration
@EnableIntegration // Inicia el sustena de gestion de flujos
@EnableScheduling // Permute activar triggers temporales.
open class IntegrationConfig {

    /*
    @Bean
    open fun pubSubChannel(): PublishSubscribeChannel {
        return MessageChannels.publishSubscribe().get()
    }
    */
    @Bean
    open fun pubSubChannel(): PublishSubscribeChannelSpec<*> = MessageChannels.publishSubscribe()


    /*
    @Bean
    fun qrFlow(pubSubChannel: PublishSubscribeChannel) = IntegrationFlows
        .from(pubSubChannel)
        .filter { message -> 
            message.headers["type"] == "qr" 
        }
        .handle { payload, headers ->
            println("QR Consumer received: $payload")
        }
        .get()

    @Bean
    fun brandedFlow(pubSubChannel: PublishSubscribeChannel) = IntegrationFlows
        .from(pubSubChannel)
        .filter { message -> 
            message.headers["type"] == "branded"
        }
        .handle { payload, headers ->
            println("Branded Consumer received: $payload")
        }
        .get()
    */
}