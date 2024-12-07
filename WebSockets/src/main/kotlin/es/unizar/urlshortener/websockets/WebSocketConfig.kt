package es.unizar.urlshortener.websockets

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSocketHandler(), "/ws-endpoint") // endpoint WebSocket al que los clientes pueden conectarse
            .setAllowedOrigins("*") // Permitir conexiones desde cualquier origen
    }

    fun webSocketHandler(): WebSocketHandler {
        return SocketHandler() // El manejador de los mensajes WebSocket
    }
}
