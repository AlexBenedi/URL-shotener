package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.User
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.GetUserInformationUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
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
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
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
    val processUser : GetUserInformationUseCase
) : UrlShortenerController {

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
                sponsor = data.sponsor
            )
        ).run {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(hash, request) }.toUri()
            h.location = url
            val response = ShortUrlDataOut(
                url = url,
                properties = mapOf(
                    "safe" to properties.safe
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
        processUser.processUser(user)

        val links = processUser.getLinks(user)

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


}

