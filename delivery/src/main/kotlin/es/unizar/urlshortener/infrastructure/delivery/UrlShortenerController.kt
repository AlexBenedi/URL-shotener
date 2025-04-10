@file:Suppress("LongParameterList", "LongMethod", "ReturnCount", "WildcardImport")
package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.GetUserInformationUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URI
import java.util.Base64
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCase
import es.unizar.urlshortener.core.usecases.DeleteUserLinkUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.OffsetDateTime
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * The specification of the controller.
 */
interface UrlShortenerController {
    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Unit>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortenerUser(data: ShortUrlDataIn, request: HttpServletRequest,userId:String): ResponseEntity<ShortUrlDataOut>

    /**
     * This method is used to get the user information.
     * @param tocken the user information
     * @return the user information
     */
    fun user(token: OAuth2AuthenticationToken?): ResponseEntity<String>

    /**
     * Retrieves the QR code image associated with a short URL identified by its [id].
     *
     * @param id The identifier of the short URL.
     * @return The QR code as a downloadable image.
     */
    fun getQRCode(id: String): ResponseEntity<ByteArray>

    fun getUserLinks(userId: String): ResponseEntity<List<Link>>

    fun getClicksByHash(@PathVariable hash: String): ResponseEntity<Int>

    fun deleteLink(@PathVariable idLink: Long): ResponseEntity<String>
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val isBranded: Boolean? = null,
    val name: String? = null,
    val sponsor: String? = null,
    val generateQRCode : Boolean? = null // Field to know if the client wants to generate a QR code
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val qrCodeGenerated: Boolean? = null,
    val urlQR: String? = null,
    val properties: Map<String, Any> = emptyMap(),
    val error: String? = null
)

/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val getUserInformationUseCase : GetUserInformationUseCase,
    val deleteUserLinkUseCase : DeleteUserLinkUseCase,
    val generateQRCodeUseCase: GenerateQRCodeUseCase,
    val shortUrlRepositoryService: ShortUrlRepositoryService,


) : UrlShortenerController {
    @Value("\${redirection.limit}")
    private var redirectionLimitConfig: Int = 0
    @Value("\${app.sync-mode}")
    private var isSyncMode: Boolean = false
    private val ipRedirectionCounts = ConcurrentHashMap<String, Pair<Int, Instant>>()
    companion object {
        private const val REDIRECTION_LIMIT = 6 // Cambiada a const val
        private val TIME_WINDOW_SECONDS = TimeUnit.HOURS.toSeconds(1)
        private const val MINUTES_LIMIT = 60
    }
    @Value("\${server.ip}")
    private lateinit var serverIp: String
    /**
     * Redirects and logs a short url identified by its [id].
     *
     * @param id the identifier of the short url
     * @param request the HTTP request
     * @return a ResponseEntity with the redirection details
     */
    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Unit> =
        redirectUseCase.redirectTo(id).run {
            logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
            val h = HttpHeaders()
            h.location = URI.create(target)
            ResponseEntity<Unit>(h, HttpStatus.valueOf(mode))
        }

    /**
     * Creates a short url from details provided in [data].
     *
     * @param data the data required to create a short url
     * @param request the HTTP request
     * @return a ResponseEntity with the created short url details
     */
    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> {
        val ip = request.remoteAddr
        println("IP: $ip")
        val now = Instant.now()

        val (currentCount, lastTimestamp) = ipRedirectionCounts[ip] ?: Pair(0, now)

        // Check if the time window has expired
        if (now.epochSecond - lastTimestamp.epochSecond > TIME_WINDOW_SECONDS) {
            ipRedirectionCounts[ip] = Pair(1, now) // Reset the count
        } else if (currentCount >= REDIRECTION_LIMIT && redirectionLimitConfig != -1) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ShortUrlDataOut(error = "Too many requests from this IP. Try again later."))
        } else {
            ipRedirectionCounts[ip] = Pair(currentCount + 1, lastTimestamp) // Increment the count
        }

        return createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = ip,
                sponsor = data.sponsor,
                isBranded = data.isBranded,
                generateQrCode = data.generateQRCode,
                name = data.name
            )
        ).run {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(hash, request) }.toUri()
            h.location = url

            val urlForQR = if(data.generateQRCode == true) {
                "http://${serverIp}/qr/${hash}"
            } else {
                null
            }
            val response = ShortUrlDataOut(
                url = url,
                qrCodeGenerated = data.generateQRCode, // Assign the QR code if generated
                urlQR = urlForQR,
                properties = mapOf(
                    "safe" to mapOf(
                        "isSafe" to properties.safe?.isSafe,
                        "threatType" to properties.safe?.threatType,
                        "platformType" to properties.safe?.platformType,
                        "threatEntryType" to properties.safe?.threatEntryType,
                        "threatInfo" to properties.safe?.threatInfo
                    )
                )
            )
            println(response)
            ResponseEntity(response, h, HttpStatus.CREATED)
        }
    }

    /**
     * Creates a short url from details provided in [data].
     *
     * @param data the data required to create a short url
     * @param request the HTTP request
     * @param userId the ID of the user
     * @return a ResponseEntity with the created short url details
     */
    @PostMapping("/api/link/user/{userId}", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortenerUser(
        @ModelAttribute data: ShortUrlDataIn, 
        request: HttpServletRequest, 
        @PathVariable userId: String
    ): ResponseEntity<ShortUrlDataOut> {

        val user1 = getUserInformationUseCase.findById(userId)

        if (user1 != null) {
            val currentTime = OffsetDateTime.now()
            val lastRedirectionTime = user1.lastRedirectionTimeStamp ?: OffsetDateTime.MIN
            val timeElapsed = Duration.between(lastRedirectionTime, currentTime).toMinutes()

            var userRedirections = user1.redirections
            if (timeElapsed >= MINUTES_LIMIT) {
                userRedirections = 0 // Restablece las redirecciones cada 60 minutos
            }

            if (redirectionLimitConfig != - 1 && userRedirections >= redirectionLimitConfig) {
                // Return 429 Too Many Requests
                return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS) // Set HTTP status to 429
                    .body(
                        ShortUrlDataOut(
                            url = null,
                            properties = mapOf("error" to "Too many requests. Please try again later.")
                        )
                    )
            } else {
                val newRedirections = user1.redirections + 1

                // Crear el ShortUrl con el use case
                val shortUrlCreation = createShortUrlUseCase.createAndDoNotSave(
                    url = data.url,
                    data = ShortUrlProperties(
                        ip = request.remoteAddr,
                        sponsor = data.sponsor,
                        isBranded = data.isBranded,
                        generateQrCode = data.generateQRCode,
                        name = data.name
                    ),
                    userId = userId,
                    isSyncMode = isSyncMode
                )

                val shortUrl = ShortUrl(
                    hash = shortUrlCreation.hash,
                    redirection = Redirection(target = data.url),
                    created = OffsetDateTime.now(),
                    properties = shortUrlCreation.properties
                )

                val user = User(
                    userId = userId,
                    redirections = newRedirections,
                    lastRedirectionTimeStamp = OffsetDateTime.now()
                )

                getUserInformationUseCase.save(user)

                // Crear el objeto Click (o recuperarlo si ya tienes la información en algún otro lugar)
                val click = Click(
                    hash = shortUrlCreation.hash,
                    properties = ClickProperties(
                        ip = request.remoteAddr,
                        referrer = request.getHeader("referer"),
                        browser = request.getHeader("user-agent"),
                        platform = request.getHeader("user-platform"),
                    ),  // Ajusta esto según tu lógica
                    created = shortUrl.created,
                    clicks = 0
                )

                // Crear el objeto Link usando el ShortUrl y el Click
                val link = Link(
                    click = click,
                    shortUrl = shortUrl,
                    user = user,
                    id = null
                )

                getUserInformationUseCase.saveLink(link, isSyncMode)

                // Build the response
                val shortenedUrl = "${request.scheme}://${request.serverName}:${request.serverPort}/${shortUrl.hash}"
                val urlForQR = if(data.generateQRCode == true) {
                    "http://${serverIp}/qr/${shortUrl.hash}"
                } else {
                    null
                }
                val response = ShortUrlDataOut(
                    url = URI.create(shortenedUrl), // Convert String to URI
                    qrCodeGenerated = data.generateQRCode, // Assign the QR code if generated
                    urlQR = urlForQR,
                    properties = mapOf("message" to "Link created successfully")
                )
                println(response)
                return ResponseEntity.ok(response)
            }
        } else {
            // Return 404 Not Found
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // Set HTTP status to 404
                .body(
                    ShortUrlDataOut(
                        url = null,
                        properties = mapOf("error" to "User not found")
                    )
                )
        }
    }

    /**
     * Handles the GET request to retrieve user links.
     *
     * @param userId The ID of the user whose links are to be retrieved.
     * @return A [ResponseEntity] containing a list of [Link] objects associated with the user.
     */
    @GetMapping("/api/users/{userId}/links")
    @ResponseBody
    override fun getUserLinks(@PathVariable userId: String): ResponseEntity<List<Link>> {
        System.out.println("UserId from getUserLinks : $userId")
        val user = getUserInformationUseCase.findById(userId)
        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyList())
        }
        val links = user?.let { getUserInformationUseCase.getLinks(it) }
        return ResponseEntity.ok(links)
    }

    /**
     * Handles the GET request to retrieve the client's IP address.
     *
     * @param request The [HttpServletRequest] object containing the client's request information.
     * @return A [ResponseEntity] containing the client's IP address as a [String].
     */
    @GetMapping("/api/ip")
    fun getClientIp(request: HttpServletRequest): ResponseEntity<String> {
        val ip = request.remoteAddr
        return ResponseEntity.ok(ip)
    }

    /**
     * Handles the GET request to retrieve the user page.
     *
     * @param token The [OAuth2AuthenticationToken] containing the user's authentication information.
     * @return A [ResponseEntity] containing the user page HTML with the user ID embedded.
     */
    @GetMapping("/user")
    @ResponseBody
    override fun user(token: OAuth2AuthenticationToken?): ResponseEntity<String> {
        if(token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated")
        }
        val user = User(token.principal.attributes["sub"].toString(), redirections = 0, lastRedirectionTimeStamp = null)
        val userId = user.userId
        getUserInformationUseCase.processUser(user)

        println("User ID from users: $userId")

        val resource = ClassPathResource("static/user.html")
        var htmlContent = resource.inputStream.bufferedReader().use { it.readText() }

        // Reemplazar un marcador en el HTML (por ejemplo, {{userId}})
        htmlContent = htmlContent.replace("{{userId}}", userId)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            .body(htmlContent)
    }

    /**
     * Handles the GET request to retrieve the total number of clicks for a given hash.
     *
     * @param hash The hash of the shortened URL.
     * @return A [ResponseEntity] containing the total number of clicks as an [Int].
     */
    @GetMapping("/clicks/{hash}")
    override fun getClicksByHash(@PathVariable hash: String): ResponseEntity<Int> {
        val totalClicks = logClickUseCase.getTotalClicksByHash(hash) ?: 0
        return ResponseEntity.ok(totalClicks)
    }

    /**
     * Handles the DELETE request to delete a link by its ID.
     *
     * @param idLink The ID of the link to be deleted.
     * @return A [ResponseEntity] containing a success message if the link is deleted successfully,
     *         or an error message if the deletion fails.
     */
    @DeleteMapping("/delete/{idLink}")
    override fun deleteLink(@PathVariable idLink: Long): ResponseEntity<String> {
        return try {
            deleteUserLinkUseCase.deleteById(idLink)
            ResponseEntity.ok("Link con id $idLink eliminado con éxito.")
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al eliminar el link: ${ex.message}")
        }
    }

    /**
     * Handles the GET request to retrieve the QR code image for a given short URL ID.
     *
     * @param id The ID of the short URL.
     * @return A [ResponseEntity] containing the QR code image as a [ByteArray] in PNG format,
     *         or a 404 status if the short URL is not found, or a 500 status if there is an error decoding the QR code.
     */
    @GetMapping("/qr/{id}", produces = [MediaType.IMAGE_PNG_VALUE])
    override fun getQRCode(
        @PathVariable id: String
    ): ResponseEntity<ByteArray> {
        val shortUrl = shortUrlRepositoryService.findByKey(id)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)

        val qrCodeBase64 = if (shortUrl.qrCode != null) {
            shortUrl.qrCode
        } else {
            // Usar el target proporcionado para generar el QR
            val generatedQRCode = generateQRCodeUseCase.generateQRCode("http://localhost:8080/" + id).base64Image
            val updatedShortUrl = shortUrl.copy(qrCode = generatedQRCode)
            shortUrlRepositoryService.save(updatedShortUrl)
            generatedQRCode
        }

        return try {
            val qrCodeImage = Base64.getDecoder().decode(qrCodeBase64)
            ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage)
        } catch (e: IllegalArgumentException) {
            println("Error decoding Base64 QR code: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

