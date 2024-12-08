package es.unizar.urlshortener.websockets

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter

@Configuration
open class WebSocketsConfig {

    @Bean
    open fun serverEndpointExporter(): ServerEndpointExporter {
        return ServerEndpointExporter()
    }
}