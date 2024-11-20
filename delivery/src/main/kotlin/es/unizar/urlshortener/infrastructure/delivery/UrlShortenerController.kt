package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
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
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCaseImpl
import es.unizar.urlshortener.core.usecases.DeleteUserLinkUseCase
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import org.springframework.core.io.ClassPathResource
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import es.unizar.urlshortener.core.User
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.Click

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
    fun shortenerUser(data: ShortUrlDataIn, request: HttpServletRequest,userId:String): ResponseEntity<ByteArray>

    /**
     * This method is used to get the user information.
     * @param tocken the user information
     * @return the user information
     */
    fun user(token: OAuth2AuthenticationToken): ResponseEntity<String>

    /**
     * Retrieves the QR code image associated with a short URL identified by its [id].
     *
     * @param id The identifier of the short URL.
     * @return The QR code as a downloadable image.
     */
    fun getQRCode(id: String): ResponseEntity<ByteArray>

    fun getUserLinks(userId: String): ResponseEntity<List<Link>>

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
    val qrCode: String? = null, // Add the QR code here as a separate field
    val properties: Map<String, Any> = emptyMap()
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
    private val shortUrlRepositoryService: ShortUrlRepositoryService
) : UrlShortenerController {

    // Directly instantiate the QR Code use case implementation
    private val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl(shortUrlRepositoryService)

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
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                isBranded = data.isBranded,
                name = data.name
            )
        ).run {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(hash, request) }.toUri()
            h.location = url

            // Check if the QR code should be generated
            val qrCode = if (data.generateQRCode == true) {
                generateQRCodeUseCase.generateQRCode(url.toString(), hash).base64Image
            } else {
                null
            }

            val response = ShortUrlDataOut(
                url = url,
                qrCode = qrCode, // Assign the QR code if generated
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
            ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
        }

    /**
     * Creates a short url from details provided in [data].
     *
     * @param data the data required to create a short url
     * @param request the HTTP request
     * @return a ResponseEntity with the created short url details
     */
    @PostMapping("/api/linkUser", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortenerUser(data: ShortUrlDataIn, request: HttpServletRequest, @RequestParam userId:String): ResponseEntity<ByteArray> {
        System.out.println("UserId from shortenerUser : $userId")
        System.out.println("URL from shortenerUser : ${data.url}")

        // Crear el ShortUrl con el use case
        val shortUrl = createShortUrlUseCase.createAndDoNotSave(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                isBranded = data.isBranded,
                name = data.name
            )
        )

        System.out.println("Short hash  : ${shortUrl.hash}")

        // Crear el objeto Click (o recuperarlo si ya tienes la información en algún otro lugar)
        val click = Click(
            hash = shortUrl.hash,
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
            userId = userId
        )

        getUserInformationUseCase.saveLink(link)

        generateQRCodeUseCase.generateQRCode(data.url, shortUrl.hash).base64Image


        val message = "El enlace se creó y guardó correctamente."
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE) // Indicamos que el contenido es texto plano
            .body(message.toByteArray(Charsets.UTF_8))
    }

    @GetMapping("/api/getUserLink")
    @ResponseBody
    override fun getUserLinks(@RequestParam userId: String): ResponseEntity<List<Link>> {
        System.out.println("UserId from getUserLinks : $userId")
        val user = User(userId)
        val links = getUserInformationUseCase.getLinks(user)
        return ResponseEntity.ok(links)
    }


    @GetMapping("/user")
    @ResponseBody
    override fun user(token: OAuth2AuthenticationToken): ResponseEntity<String> {
        val user = User(token.principal.attributes["sub"].toString())
        val userId = user.userId
        getUserInformationUseCase.processUser(user)

        System.out.println("User ID from users: $userId")

        val resource = ClassPathResource("static/user.html")
        var htmlContent = resource.inputStream.bufferedReader().use { it.readText() }

        // Reemplazar un marcador en el HTML (por ejemplo, {{userId}})
        htmlContent = htmlContent.replace("{{userId}}", userId)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            .body(htmlContent)
    }


    @GetMapping("/{id}/qr", produces = [MediaType.IMAGE_PNG_VALUE])
    override fun getQRCode(@PathVariable id: String): ResponseEntity<ByteArray> {
        val shortUrl = shortUrlRepositoryService.findByKey(id)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        //Show in the console the hash of the short URL
        println("The hash of the short URL is: $id")
        //Show in the console the value of the qrCode field
        println("The value of the qrCode field is: ${shortUrl.properties.qrCode}")

        val qrCodeBase64 = shortUrl.properties.qrCode

        // Check if the QR code is present
        // Decode the Base64 string into a byte array
        try {
            val qrCodeImage = Base64.getDecoder().decode(qrCodeBase64)
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage)
        } catch (e: IllegalArgumentException) {
            println("Error decoding Base64 QR code: ${e.message}")
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

