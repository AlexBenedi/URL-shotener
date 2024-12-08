package es.unizar.urlshortener.websockets


import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class SocketHandler : TextWebSocketHandler() {
    private val sessions = mutableMapOf<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = session.principal?.name
        if (user != null) {
            sessions[user] = session // Mapea el usuario a la sesión WebSocket
            println("Conexión establecida para el usuario: $user")
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

        println("Mensaje recibido: ${message.payload}")
    }

    fun sendMessageToUser(user: String, message: String) {
        val session = sessions[user]
        session?.sendMessage(TextMessage(message))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        val user = session.principal?.name
        if (user != null) {
            sessions.remove(user)
            println("Sesión cerrada para el usuario: $user")
        }
    }
}