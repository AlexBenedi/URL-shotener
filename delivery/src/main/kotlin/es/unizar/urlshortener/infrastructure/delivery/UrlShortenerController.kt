package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.User
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.usecases.GetUserInformationUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import org.springframework.web.bind.annotation.RequestParam
import java.io.ByteArrayOutputStream
import java.util.Base64
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCase
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCaseImpl
import es.unizar.urlshortener.core.usecases.DeleteUserLinkUseCase
import es.unizar.urlshortener.core.usecases.DeleteUserLinkUseCaseImpl
import es.unizar.urlshortener.core.ShortUrlRepositoryService


import java.security.Principal;

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
     * This method is used to get the user information.
     * @param tocken the user information
     * @return the user information
     */
     fun user(token: OAuth2AuthenticationToken): Map<String, Any>

    /**
     * Retrieves the QR code image associated with a short URL identified by its [id].
     *
     * @param id The identifier of the short URL.
     * @return The QR code as a downloadable image.
     */
    fun getQRCode(id: String): ResponseEntity<ByteArray>

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
    val deleteUserLinkUseCase : DeleteUserLinkUseCase
    private val shortUrlRepositoryService: ShortUrlRepositoryService
) : UrlShortenerController {

    // Directly instantiate the QR Code use case implementation
    private val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl( shortUrlRepositoryService )

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
     * This method is used to get the user information.
     * @param principal the user information
     * @return the user information
     */
    @GetMapping("/user")
    override fun user(token: OAuth2AuthenticationToken): Map<String, Any> {
        // Crear el usuario a partir del token de autenticación OAuth2
        val user = User(token.principal.attributes["sub"].toString())

        // Obtener los atributos del token (nombre y correo)
        val name = token.principal.attributes["name"].toString()
        val email = token.principal.attributes["email"].toString()

        // Llamar a processUser para obtener los enlaces asociados al usuario
        getUserInformationUseCase.processUser(user)

        val links = getUserInformationUseCase.getLinks(user)

        //Insertar el link obtenido otra vez

        // Convertir los links en una representación adecuada (puede ser una lista de strings, JSON, etc.)
        val linkInfo = links.map { link ->
            mapOf(
                "click" to mapOf(
                    "hash" to link.click.hash,
                    "properties" to link.click.properties,
                    "created" to link.click.created
                ),
                "shortUrl" to mapOf(
                    "hash" to link.shortUrl.hash,
                    "redirection" to link.shortUrl.redirection.target,
                    "created" to link.shortUrl.created,
                    "properties" to link.shortUrl.properties
                ),
                "userId" to link.userId
            )
        }

        // Devolver toda la información como un mapa
        return mapOf(
            "user" to mapOf(
                "id" to user.userId,
                "name" to name,
                "email" to email
            ),
            "links" to linkInfo
        )
    }

    @GetMapping("/{id}/qr", produces = [MediaType.IMAGE_PNG_VALUE])
    override fun getQRCode(@PathVariable id: String): ResponseEntity<ByteArray> {
        val shortUrl = shortUrlRepositoryService.findByKey(id)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        //Show in the console the hash of the short URL
        println("The hash of the short URL is: $id")
        //Show in the console the value of the qrCode field
        println("The value of the qrCode field is: ${shortUrl.properties.qrCode}")

        // Check if the QR code is present
        val qrCodeBase64 = shortUrl.properties.qrCode

        // Decode the Base64 string into a byte array
        val qrCodeImage = Base64.getDecoder().decode(qrCodeBase64)
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(qrCodeImage)
    }

}

