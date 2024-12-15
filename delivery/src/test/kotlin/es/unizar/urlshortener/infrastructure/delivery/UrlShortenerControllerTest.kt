@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.*
import org.hamcrest.CoreMatchers.containsString
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.*
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.Test


@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerImpl::class,
        GenerateQRCodeUseCaseImpl::class,
        RestResponseEntityExceptionHandler::class
    ]
)
@Suppress("UnusedPrivateProperty")
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    @MockBean
    private lateinit var getUserInformationUseCase: GetUserInformationUseCase

    @MockBean
    private lateinit var deleteUserLinkUseCase: DeleteUserLinkUseCase

    @MockBean
    private lateinit var securityFilterChain: SecurityFilterChain

    @MockBean 
    private lateinit var generateQRCodeUseCase: GenerateQRCodeUseCase

    @MockBean
    private lateinit var shortUrlRepositoryService: ShortUrlRepositoryService

    /**
     * Test that verifies that the `clicks` method returns the total number of clicks for a valid hash.
     */
    @Test
    fun `should return total clicks for a valid hash`() {
        val hash = "valid-hash"
        val totalClicks = 42

        // Configuramos el comportamiento del caso de uso
        whenever(logClickUseCase.getTotalClicksByHash(hash)).thenReturn(totalClicks)

        mockMvc.perform(get("/clicks/{hash}", hash))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string("42")) // Verifica que el número de clics se devuelve correctamente
    }

    /**
     * Test that verifies that the `getUserLink` method returns a list of links for a valid user.
     */
    @Test
    fun `should return links for a valid user`() {
        val userId = "valid-user-id"
        val user = User(userId = userId, redirections = 2, lastRedirectionTimeStamp = OffsetDateTime.now())

        val links = listOf(
            Link(Click(hash = "anyone", properties = ClickProperties(null), created = OffsetDateTime.now(),
                clicks = 0),
                ShortUrl(hash = "anyone", redirection = Redirection(target = "http://example.com"),
                    properties = ShortUrlProperties(name = "http://exampleshortened/anyone")), id = 12, user)
        )

        // Configuramos el comportamiento del caso de uso
        whenever(getUserInformationUseCase.findById(userId)).thenReturn(user)
        whenever(getUserInformationUseCase.getLinks(user)).thenReturn(links)

        // Realizamos la petición y verificamos los resultados
        mockMvc.perform(get("/api/users/{userId}/links", userId))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1)) // Verifica que hay un enlace
            .andExpect(jsonPath("$[0].id").value(12))
    }

    /**
     * Test that verifies that the `getUserLink` method returns a 404 Not Found when the user does not exist.
     */
    @Test
    fun `should return empty list if user is not found`() {
        val userId = "invalid-user-id"

        // Simulamos que el usuario no existe
        whenever(getUserInformationUseCase.findById(userId)).thenReturn(null)

        mockMvc.perform(get("/api/users/{userId}/links", userId))
            .andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0)) // Verifica que la lista está vacía
    }

    @Test
    fun `should return unauthorized when token is null`() {
        // Realizamos la solicitud al endpoint "/user" sin enviar un token
        mockMvc.perform(get("/user"))
            .andExpect(status().isUnauthorized) // Verificamos que devuelve 401 Unauthorized
    }




    /**
     * Test that verifies that we can`t access the user page without authentication.
     */
    @Test
    fun `should allow access to protected routes with authentication`() {
        // Simulamos un OAuth2Principal con un atributo "sub" que es el user ID
        val attributes = mapOf<String, Any>("sub" to "mock-user-id")
        val principal = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")), // Simulamos los roles del usuario
            attributes,
            "sub" // El nombre del atributo que contiene el user ID
        )

        // Creamos el OAuth2AuthenticationToken manualmente
        val authenticationToken = OAuth2AuthenticationToken(principal, principal.authorities,
            "google")

        // Realizamos la solicitud con MockMvc usando el token simulado
        mockMvc.perform(get("/user")
            .principal(authenticationToken))  // Pasamos el token como principal
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(containsString("mock-user-id")))
    }

    /**
     * Test that verifies that the `shortenerUser` method returns a shortened link successfully.
     */
    @Test
    fun `shortenerUser creates and returns shortened link successfully`() {
        val user = User(
            userId = "user123",
            redirections = 0,
            lastRedirectionTimeStamp = OffsetDateTime.now().minusMinutes(61) // Límite reiniciado
        )
        val shortUrl = ShortUrl(
            hash = "abc123",
            redirection = Redirection(target = "http://example.com"),
            created = OffsetDateTime.now(),
            properties = ShortUrlProperties(ip = "127.0.0.1")
        )

        given(getUserInformationUseCase.findById("user123")).willReturn(user)
        given(createShortUrlUseCase.save(shortUrl)).willReturn(shortUrl)
        given(createShortUrlUseCase.create("http://example.com", shortUrl.properties)).willReturn(shortUrl)
        given(createShortUrlUseCase.createAndDoNotSave("http://example.com", shortUrl.properties,
            "user123",
            false))
            .willReturn(shortUrl)

        mockMvc.perform(
            post("/api/link/user/{userId}", "user123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("url", "http://example.com")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value("http://localhost:80/abc123"))
    }


    /**
     * Test that verifies that the `shortenerUser` method returns a 404 Not Found when the user does not exist.
     */
    @Test
    fun `shortenerUser returns 404 Not Found when user does not exist`() {
        given(getUserInformationUseCase.findById("invalidUserId")).willReturn(null)

        mockMvc.perform(
            post("/api/link/user/{userId}", "invalidUserId")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("url", "http://example.com")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.properties.error").value("User not found"))
    }

    /**
     * Test that verifies that the `shortenerUser` method returns a shortened link successfull when
     * the user wants also a QR code.
     */
    @Test
    fun `shortenerUser generates QR code and returns shortened link successfully`() {
        val user = User(
            userId = "user123",
            redirections = 0,
            lastRedirectionTimeStamp = OffsetDateTime.now().minusMinutes(61) // Límite reiniciado
        )
        val shortUrl = ShortUrl(
            hash = "abc123",
            redirection = Redirection(target = "http://example.com"),
            created = OffsetDateTime.now(),
            properties = ShortUrlProperties(
                ip = "127.0.0.1",
                generateQrCode = true
            )
        )

        // Simula que el usuario existe
        given(getUserInformationUseCase.findById("user123")).willReturn(user)

        given(createShortUrlUseCase.save(shortUrl)).willReturn(shortUrl)
        given(createShortUrlUseCase.create("http://example.com", shortUrl.properties)).willReturn(shortUrl)
        given(createShortUrlUseCase.createAndDoNotSave("http://example.com", shortUrl.properties,
            "user123", false))
            .willReturn(shortUrl)

        mockMvc.perform(
            post("/api/link/user/{userId}", "user123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("url", "http://example.com")
                .param("generateQRCode", "true")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value("http://localhost:80/abc123"))
            .andExpect(jsonPath("$.qrCodeGenerated").value(true))
    }


    /**
     * Test that verifies that the `shortenerUser` method returns a 429 when the user has exceeded the limit
     * of redirections.
     */
    @Test
    fun `shortenerUser returns 429 Too Many Requests when redirection limit is exceeded`() {
        val user = User(
            userId = "user123",
            redirections = 6,
            lastRedirectionTimeStamp = OffsetDateTime.now().minusMinutes(10) // Límite no reiniciado
        )
        given(getUserInformationUseCase.findById("user123")).willReturn(user)

        mockMvc.perform(
            post("/api/link/user/{userId}", "user123")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("url", "http://example.com")
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.properties.error").value("Too many requests. " +
                    "Please try again later."))
    }

    /**
     * Test that verifies that the `getLink` method returns a link when the id exists.
     */
    @Test
    fun `deleteLink removes the link when the id exists`() {
        // Stub para que el método no haga nada
        doNothing().`when`(deleteUserLinkUseCase).deleteById(1L)

        // Realiza la petición DELETE y verifica la respuesta
        mockMvc.perform(delete("/delete/{idLink}", 1L))
            .andExpect(status().isOk)
            .andExpect(content().string("Link con id 1 eliminado con éxito."))

        // Verifica que se llamó al método deleteById
        verify(deleteUserLinkUseCase).deleteById(1L)
    }

    /**
    * Test that verifies that the deleteLink method returns a bad request when an invalid id is provided.
    */
    @Test
    fun `deleteLink returns bad request when an invalid id is provided`() {
        // Stub para lanzar IllegalArgumentException
        doThrow(IllegalArgumentException("El ID proporcionado no es válido.")).`when`(deleteUserLinkUseCase)
            .deleteById(1L)

        // Realiza la petición DELETE y verifica la respuesta
        mockMvc.perform(delete("/delete/{idLink}", 1L))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Error al eliminar el link: El ID proporcionado no es válido."))

        // Verifica que se llamó al método deleteById
        verify(deleteUserLinkUseCase).deleteById(1L)
    }


    /**
     * Tests that `redirectTo` returns a redirect when the key exists.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {

        //Mock the behavior of securityFilterChain to return a SecurityFilterChain object
        //given(securityFilterChain.toString()).willReturn("SecurityFilterChain")

        //Mock the behavior of deleteUserLinkUseCase to return a DeleteUserLinkUseCase object
        //given(deleteUserLinkUseCase.toString()).willReturn("DeleteUserLinkUseCase")

        // Mock the behavior of redirectUseCase to return a redirection URL
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))

        // Perform a GET request and verify the response status and redirection URL
        mockMvc.perform(get("/{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        // Verify that logClickUseCase logs the click with the correct IP address
        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    /**
     * Tests that `redirectTo` returns a not found status when the key does not exist.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        // Mock the behavior of redirectUseCase to throw a RedirectionNotFound exception
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        // Perform a GET request and verify the response status and error message
        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))

        // Verify that logClickUseCase does not log the click
        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    /**
     * Tests that `creates` returns a basic redirect if it can compute a hash.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {

        given(generateQRCodeUseCase.toString()).willReturn("GenerateQRCodeUseCase")
        
        // Mock the behavior of createShortUrlUseCase to return a ShortUrl object
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://example.com/")))

        given(createShortUrlUseCase.findByKey("f684a3c4"))
            .willReturn(ShortUrl("f684a3c4", Redirection("http://example.com/")))

        // Perform a POST request and verify the response status, redirection URL, and JSON response
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }

    /**
     * Tests that `creates` returns a bad request status if it cannot compute a hash.
     */
    @Test
    fun `creates returns bad request if it can compute a hash`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidUrlException
        given(
            createShortUrlUseCase.create(
                url = "ftp://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1")
            )
        ).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "ftp://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.statusCode").value(400))
    }

    /**
     * Tests that `creates` returns a basic redirect if it can create a branded linkk.
     */
    @Test
    fun `creates returns a basic redirect if it can create a branded link`() {
        // Mock the behavior of createShortUrlUseCase to return a ShortUrl object
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1", name = "test", isBranded = true)
            )
        ).willReturn(ShortUrl("test", Redirection("http://example.com/")))

        given(createShortUrlUseCase.findByKey("test"))
            .willReturn(ShortUrl("test", Redirection("http://example.com/")))

        // Perform a POST request and verify the response status, redirection URL, and JSON response
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .param("name", "test")
                .param("isBranded", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/test"))
            .andExpect(jsonPath("$.url").value("http://localhost/test"))
    }

    /**
     * Tests that `creates` returns a bad request status if name have not been introduced.
     */
    @Test
    fun `creates returns bad request if name have not been introduced`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidNameBrandedUrl
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = "127.0.0.1", isBranded = true)
            )
        ).willAnswer { throw EmptyNameBrandedUrl() }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .param("isBranded", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.statusCode").value(400))
    }

    /**
     * Tests that expect to recieve the QR code of a short URL from de database
     */
    @Test
    fun `getQRCode returns existing QR code when qrCode is not null`() {
        val id = "abc123"
        val qrCodeBase64 = Base64.getEncoder().encodeToString("existing QR".toByteArray())
        val shortUrl = ShortUrl(
            hash = id,
            redirection = Redirection(target = "http://example.com"),
            created = OffsetDateTime.now(),
            properties = ShortUrlProperties(),
            qrCode = qrCodeBase64
        )

        given(shortUrlRepositoryService.findByKey(id)).willReturn(shortUrl)

        mockMvc.perform(get("/qr/$id").accept(MediaType.IMAGE_PNG))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(Base64.getDecoder().decode(qrCodeBase64)))
    }

    /**
     * Test that expect to create a new QR code for a short URL that does not have one
     */
    @Test
    fun `should generate and return QR code if not present in database`() {
        val id = "newId"
        val generatedQRCodeBase64 = Base64.getEncoder().encodeToString("generatedQRCode".toByteArray())
        val shortUrl = ShortUrl(
            hash = id,
            redirection = Redirection("http://example.com"),
            created = OffsetDateTime.now(),
            qrCode = null
        )
        val updatedShortUrl = shortUrl.copy(qrCode = generatedQRCodeBase64)

        given(shortUrlRepositoryService.findByKey(id)).willReturn(shortUrl)
        given(generateQRCodeUseCase.generateQRCode(any(), eq(250))
            ).willReturn(QRCode("http://localhost:8080/"+id, generatedQRCodeBase64, 250))
        given(shortUrlRepositoryService.save(updatedShortUrl)).willReturn(updatedShortUrl)

        mockMvc.perform(get("/qr/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
    }

    /**
     * Test that expect to return a 404 Not Found when the short URL does not exist
     */
    @Test
    fun `should return 404 if short URL id does not exist`() {
        val id = "nonExistentId"

        given(shortUrlRepositoryService.findByKey(id)).willReturn(null)

        mockMvc.perform(get("/qr/{id}", id))
            .andExpect(status().isNotFound)
    }

    /**
     * Test that expect to return a 500 Internal Server Error when the QR code Base64 is invalid
     */
    @Test
    fun `getQRCode returns 500 when QR code Base64 is invalid`() {
        val id = "abc123"
        val invalidBase64 = "invalid_base64_string"
        val shortUrl = ShortUrl(
            hash = id,
            redirection = Redirection(target = "http://example.com"),
            created = OffsetDateTime.now(),
            properties = ShortUrlProperties(),
            qrCode = invalidBase64
        )

        given(shortUrlRepositoryService.findByKey(id)).willReturn(shortUrl)

        mockMvc.perform(get("/qr/$id").accept(MediaType.IMAGE_PNG))
            .andExpect(status().isInternalServerError)
    }

    /**
     * Test that expect a 429 Too many requests when the user wants to create a short URL but has exceeded the limit
     * of redirections.
     */
    @Test
    fun `creates returns 429 Too Many Requests when redirection limit is exceeded`() {
        val user = User(
            userId = "user123",
            redirections = 6,
            lastRedirectionTimeStamp = OffsetDateTime.now().minusMinutes(10) // Límite no reiniciado
        )

        given(getUserInformationUseCase.findById("user123")).willReturn(user)

        mockMvc.perform(
            post("/api/link/user/{userId}", "user123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("url", "http://example.com")
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.properties.error").value("Too many requests. " +
                    "Please try again later."))
    }

}
