@file:Suppress("WildcardImport")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.time.OffsetDateTime

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface GetUserInformationUseCase {
    /**
     * Searchs if the user exists in the database, and if it does not exist, it saves it.
     */
    fun processUser(user: User)

    /**
     * Inserts a example link in the database for testing purposes.
     */
    fun insertExampleLink(user: User, hashUrl: String)

    /**
     *  Gets the linss of a user
     *
     *  @param user The user id to be saved.
     *  @return The list of links of the user
     */
    fun getLinks(user: User): List<Link>
}

/**
 * Implementation of [GetUserInformationUseCase].
 */
class GetUserInformationUseCaseImpl(
    private val userRepository : UserRepositoryService,
    private val linkRepository : LinkRepositoryService
) : GetUserInformationUseCase {

    /**
     * Searchs if the user exists in the database, and if it does not exist, it saves it.
     *
     * @param user The user id to be saved.
     * @return The created [User] entity.
     */
    override fun processUser(user: User) {
        if (safeCall {userRepository.findById(user.userId)} == null) {
            safeCall {userRepository.save(user)}
            insertExampleLink(user, user.userId)
        }
    }

    /**
     * Gets the linss of a user
     *
     * @param user The user id to be saved.
     * @return The list of links of the user
     */
    override fun getLinks(user: User): List<Link> {
        var links: List<Link> = emptyList()
        links = safeCall {linkRepository.findByUserId(user)}
        return links
    }

    /**
     * Inserts a example link in the database for testing purposes.
     *
     * @param user The user id to be saved.
     */
    override fun insertExampleLink(user: User, hashUrl : String) {
        // Datos del click
        val clickProperties = ClickProperties(
            ip = "192.168.1.1",
            referrer = "http://example.com",
            browser = "Chrome",
            platform = "Windows",
            country = "US"
        )

        val click = Click(
            hash = hashUrl,
            properties = clickProperties,
            created = OffsetDateTime.now()
        )

        // Datos del ShortUrl
        val shortUrlProperties = ShortUrlProperties(
            ip = "192.168.1.1",
            sponsor = "Sponsor",
            safe = true,
            owner = "OwnerName",
            country = "US"
        )

        val redirection = Redirection(
            target = "http://example.com",
            mode = 307
        )

        val shortUrl = ShortUrl(
            hash = hashUrl,
            redirection = redirection,
            created = OffsetDateTime.now(),
            properties = shortUrlProperties
        )

        // Crear el objeto Link
        val link = Link(
            click = click,
            shortUrl = shortUrl,
            userId = user.userId
        )

        // Guardar el link (esto debería guardar también click y shortUrl gracias a CascadeType.ALL)
        safeCall { linkRepository.save(link) }

    }


}
