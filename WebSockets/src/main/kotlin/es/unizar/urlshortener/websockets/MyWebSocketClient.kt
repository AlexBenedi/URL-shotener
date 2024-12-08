package es.unizar.urlshortener.websockets

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MyWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake) {
        println("Conexión WebSocket abierta")
    }

    override fun onMessage(message: String) {
        println("Mensaje recibido del servidor: $message")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("Conexión WebSocket cerrada: $reason")
    }

    override fun onError(ex: Exception) {
        println("Error en la conexión WebSocket: ${ex.message}")
    }
}
