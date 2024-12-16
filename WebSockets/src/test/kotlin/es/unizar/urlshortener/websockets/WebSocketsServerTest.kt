@file:Suppress("WildcardImport")
package es.unizar.urlshortener.websockets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jakarta.websocket.CloseReason
import jakarta.websocket.RemoteEndpoint
import jakarta.websocket.Session
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.Type
import java.net.URI
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@ExtendWith(MockitoExtension::class)
class WebSocketsServerTest {

    @Mock
    private lateinit var session: Session

    @Mock
    private lateinit var remoteEndpoint: RemoteEndpoint.Basic

    @InjectMocks
    private lateinit var webSocketsServer: WebSocketsServer

    @Captor
    private lateinit var messageCaptor: ArgumentCaptor<String>

    private val outContent = ByteArrayOutputStream()
    private val originalOut = System.out

    @BeforeEach
    fun setUp() {
        lenient().`when`(session.basicRemote).thenReturn(remoteEndpoint)
    }

    /**
     * Tests that the `onOpen` method adds the session to the sessions map.
     *
     * Mocks the session's request URI to include a user ID and verifies that the session is added to the sessions map.
     */
    @Test
    fun `onOpen should add session to sessions map`() {
        // Arrange
        val userId = "testUser"
        `when`(session.requestURI).thenReturn(URI("ws://localhost:8080/ws-endpoint?userId=$userId"))

        // Act
        webSocketsServer.onOpen(session)

        // Assert
        assert(WebSocketsServer.sessions.containsKey(userId))
        assert(WebSocketsServer.sessions[userId] == session)
    }

    /**
     * Tests that the `onMessage` method processes the message and sends a response.
     *
     * Mocks a WebSocket message and verifies that the response message contains the expected content.
     */
    @Test
    fun `onMessage should process message and send response`() {
        // Arrange
        val userId = "testUser"
        val messageContent = "testMessage"
        val message = Gson().toJson(WebSocketMessage(userId, messageContent))
        WebSocketsServer.sessions[userId] = session

        // Act
        webSocketsServer.onMessage(message, session)

        // Assert
        verify(remoteEndpoint).sendText(messageCaptor.capture())
        assert(messageCaptor.value.contains("Respuesta desde el servidor"))
    }

    /**
     * Tests that the `onClose` method removes the session from the sessions map.
     *
     * Mocks the closure of the WebSocket session and verifies that the session is removed from the sessions map.
     */
    @Test
    fun `onClose should remove session from sessions map`() {
        // Arrange
        val userId = "testUser"
        WebSocketsServer.sessions[userId] = session
        val closeReason = CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure")

        // Act
        webSocketsServer.onClose(session, closeReason)

        // Assert
        assert(!WebSocketsServer.sessions.containsKey(userId))
    }

        
    /**
     * Tests that the `sendMessageToUser` method sends a message if the session is open.
     *
     * Mocks an open session and verifies that the message is sent.
     */
    @Test
    fun `sendMessageToUser should send message if session is open`() {
        // Arrange
        val userId = "testUser"
        val message = "testMessage"
        `when`(session.isOpen).thenReturn(true)
        WebSocketsServer.sessions[userId] = session

        // Act
        webSocketsServer.sendMessageToUser(userId, message)

        // Assert
        verify(remoteEndpoint).sendText(message)
    }

    /**
     * Tests that the `sendMessageToUser` method does not send a message if the session is not open.
     *
     * Mocks a closed session and verifies that the message is not sent.
     */
    @Test
    fun `sendMessageToUser should not send message if session is not open`() {
        // Arrange
        val userId = "testUser"
        val message = "testMessage"
        `when`(session.isOpen).thenReturn(false)
        WebSocketsServer.sessions[userId] = session

        // Act
        webSocketsServer.sendMessageToUser(userId, message)

        // Assert
        verify(remoteEndpoint, never()).sendText(message)
    }

    /**
     * Tests that the `onError` method prints error messages.
     *
     * Mocks an error during the WebSocket session and verifies that the error messages are printed.
     */
    @Test
    fun `onError should print error messages`() {
        System.setOut(PrintStream(outContent))
        // Arrange
        val sessionId = "testSessionId"
        val throwable = Throwable("Test error message")
        `when`(session.id).thenReturn(sessionId)

        // Act
        webSocketsServer.onError(session, throwable)

        // Assert
        val output = outContent.toString()
        assert(output.contains("Error en la conexión: ${throwable.message}, sessionId: $sessionId"))
        System.setOut(originalOut)
    }
}
