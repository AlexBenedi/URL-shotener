@file:Suppress("WildcardImport")
package es.unizar.urlshortener.websockets

import org.java_websocket.handshake.ServerHandshake
import org.junit.jupiter.api.*
import org.mockito.Mockito
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.URI

class MyWebSocketClientTest {

    private lateinit var client: MyWebSocketClient
    private val outContent = ByteArrayOutputStream()
    private val originalOut = System.out

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outContent))
        client = MyWebSocketClient(URI("ws://localhost:8080"))
    }

    /**
     * Cleans up the test environment after each test.
     *
     * Restores the original standard output.
     */
    @AfterEach
    fun tearDown() {
        System.setOut(originalOut)
    }

    /**
     * Tests that the `onOpen` method prints the correct connection open message.
     *
     * Mocks a `ServerHandshake` and verifies that the message "Conexión WebSocket abierta" is printed.
     */
    @Test
    fun `onOpen should print connection open message`() {
        // Arrange
        val handshake = Mockito.mock(ServerHandshake::class.java)

        // Act
        client.onOpen(handshake)

        // Assert
        val output = outContent.toString().trim()
        Assertions.assertEquals("Conexión WebSocket abierta", output)
    }

    /**
     * Tests that the `onMessage` method prints the correct received message.
     *
     * Sends a test message and verifies that the message "Mensaje recibido del servidor: Test message" 
     * is printed.
     */
    @Test
    fun `onMessage should print received message`() {
        // Arrange
        val message = "Test message"

        // Act
        client.onMessage(message)

        // Assert
        val output = outContent.toString().trim()
        Assertions.assertEquals("Mensaje recibido del servidor: $message", output)
    }

    /**
     * Tests that the `onClose` method prints the correct connection closed message.
     *
     * Mocks the closure of the WebSocket connection and verifies that the message 
     * "Conexión WebSocket cerrada: Normal closure" is printed.
     */
    @Test
    fun `onClose should print connection closed message`() {
        // Arrange
        val code = 1000
        val reason = "Normal closure"
        val remote = true

        // Act
        client.onClose(code, reason, remote)

        // Assert
        val output = outContent.toString().trim()
        Assertions.assertEquals("Conexión WebSocket cerrada: $reason", output)
    }

    /**
     * Tests that the `onError` method prints the correct error message.
     *
     * Mocks an exception and verifies that the message "Error en la conexión WebSocket: Test error" is printed.
     */
    @Test
    fun `onError should print error message`() {
        // Arrange
        val exception = Exception("Test error")

        // Act
        client.onError(exception)

        // Assert
        val output = outContent.toString().trim()
        Assertions.assertEquals("Error en la conexión WebSocket: ${exception.message}", output)
    }
}
