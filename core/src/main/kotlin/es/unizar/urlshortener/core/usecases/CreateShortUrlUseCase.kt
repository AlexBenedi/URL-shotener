@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.LimitExceededException
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.safeCall
import es.unizar.urlshortener.core.Redirection

private const val MAX_SHORTENED_URLS = 5

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     */
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService
) : CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     * @throws InvalidUrlException if the URL is not valid.
     */
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
        // Get the user ID from the data (modify as needed to get the actual user ID)
        val userId = data.sponsor ?: "anonymous" // or however you identify users

        // Check if the user has exceeded the limit
        val count = shortUrlRepository.countShortenedUrlsByUser(userId)
        if (count >= MAX_SHORTENED_URLS) {
            throw LimitExceededException("You have reached the limit of 5 shortened URLs. Please try again later.")
        }

        if (safeCall { validatorService.isValid(url) }) {
            val id = safeCall { hashService.hasUrl(url) }
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor
                )
            )
            return safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(url)
        }
    }
}
