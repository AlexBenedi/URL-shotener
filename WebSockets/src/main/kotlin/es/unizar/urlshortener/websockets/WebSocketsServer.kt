package es.unizar.urlshortener.websockets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jakarta.websocket.CloseReason
import jakarta.websocket.CloseReason.CloseCodes
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.RemoteEndpoint
import jakarta.websocket.Session
import jakarta.websocket.server.ServerEndpoint
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import java.util.Locale
import java.util.Scanner
import java.util.concurrent.ConcurrentHashMap
import java.lang.reflect.Type

@Configuration(proxyBeanMethods = false)
open class WebSocketConfig {
    @Bean
    open fun serverEndpoint() = ServerEndpointExporter()
}

/**
 * If the websocket connection underlying this [RemoteEndpoint] is busy sending a message when a call is made to send
 * another one, for example if two threads attempt to call a send method concurrently, or if a developer attempts to
 * send a new message while in the middle of sending an existing one, the send method called while the connection
 * is already busy may throw an [IllegalStateException].
 *
 * This method wraps the call to [RemoteEndpoint.Basic.sendText] in a synchronized block to avoid this exception.
 */
fun RemoteEndpoint.Basic.sendTextSafe(message: String) {
    synchronized(this) {
        sendText(message)
    }
}
@Component
@ServerEndpoint("/ws-endpoint")
class WebSocketsServer {
    companion object {
        var instanceCount = 0
        val sessions = ConcurrentHashMap<String, Session>()
    }

    init {
        instanceCount++
        println("Instancias de WebSocketsServer: $instanceCount")
    }

    // Mapa para almacenar las sesiones de los usuarios por su userId

    @OnOpen
    fun onOpen(session: Session) {
        println("Conexión abierta con: ${session.id}")

        // Extraer el userId de la URL de la sesión
        val queryParams = session.requestURI.query
        val userId = queryParams?.split("=")?.get(1)

        if (userId != null) {
            sessions[userId] = session
            val sesionPrueba = sessions[userId]
            println("Conexión establecida para userId: $userId")
            println("Valor de la sesión: $sesionPrueba")
            println("La sesión está abierta: ${sesionPrueba?.isOpen}")
        } else {
            println("No se proporcionó userId en la URL")
        }
        println("Sesiones activas: ${sessions}")
    }

    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("Mensaje recibido: $message")
        val gson = Gson()
        val tupleType: Type = object : TypeToken<Pair<String, String>>() {}.type
        val userQrCode: Pair<String, String> = gson.fromJson(message, tupleType)

        println("User ID: ${userQrCode.first}")
        println("QR Code: ${userQrCode.second}")
        
        // Aquí puedes realizar la lógica para enviar la respuesta con el QR
        println("Instancias activas kafka: $instanceCount")
        println("Sesiones activas: ${sessions}")
        sendMessageToUser(userQrCode.first, userQrCode.second)
        session.basicRemote.sendText("Respuesta desde el servidor: $message")
    }

    @OnClose
    fun onClose(session: Session, reason: CloseReason) {
        println("Conexión cerrada: ${session.id}, motivo: ${reason.reasonPhrase}")
        // Eliminar la sesión del mapa cuando se cierra
        sessions.entries.removeIf { it.value == session }
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        println("Error en la conexión: ${throwable.message}")
    }

    // Metodo para enviar el QR al usuario específico
    fun sendMessageToUser(userId: String, message: String) {
        val session = sessions[userId]
        println("Enviando mensaje al usuario: $userId")
        println("Valor de la sesión: $session")
        println("La sesión está abierta: ${session?.isOpen}")
        if (session != null && session.isOpen) {
            session.basicRemote.sendText(message)
        } else {
            println("No hay sesión activa para el usuario: $userId")
        }
    }
}





