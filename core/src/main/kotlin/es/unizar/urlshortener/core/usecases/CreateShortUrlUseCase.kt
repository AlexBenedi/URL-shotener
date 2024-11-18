@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

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
    private val hashService: HashService,
    private val safetyService: SafetyService
) : CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     * @throws InvalidUrlException if the URL is not valid, InvalidNameBrandedUrl if
     *         the name is not provided for a branded URL, or LimitExceededException if the user 
     *         has exceeded the limit of shortened URLs.
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
            /*if (!safeCall { safetyService.isUrlSafe(url) }) {
                println("URL is not safe")
                throw UnsafeUrlException(url)
            }*/
            var id = safeCall { hashService.hasUrl(url) }
            if (data.isBranded == true ) {
                if ( data.name != null ) {
                    id = data.name
                } else {
                    throw InvalidNameBrandedUrl()
                }
            }
            safeCall { safetyService.isUrlSafe(url) } // post kafka message
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = null,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    isBranded = data.isBranded != null && data.name != null,
                )
            )
            return safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(url)
        }
    }
}
