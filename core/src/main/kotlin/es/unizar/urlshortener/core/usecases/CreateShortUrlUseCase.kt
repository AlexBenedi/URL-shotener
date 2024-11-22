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

    fun createAndDoNotSave(url: String, data: ShortUrlProperties, userId : String): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService,
    private val safetyService: SafetyService,
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
            val safety = safeCall { safetyService.isUrlSafe(url) } // this must be async
            println(safety)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = safety,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    isBranded = data.isBranded != null && data.name != null,
                    qrCode = data.qrCode,
                )
            )
            return safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(url)
        }
    }

    override fun createAndDoNotSave(url: String, data: ShortUrlProperties, userId : String): ShortUrl{
        // Get the user ID from the data (modify as needed to get the actual user ID)

        if (safeCall { validatorService.isValid(url) }) {
            /*if (!safeCall { safetyService.isUrlSafe(url) }) {
                println("URL is not safe")
                throw UnsafeUrlException(url)
            }*/
            var id = safeCall { hashService.hasUrl(url) }
            id += userId
            System.out.println("ID de la url a insertar: " + id)
            if (data.isBranded == true ) {
                if ( data.name != null ) {
                    id = data.name
                } else {
                    throw InvalidNameBrandedUrl()
                }
            }
            val safety = safeCall { safetyService.isUrlSafe(url) } // this must be async
            println("Safety checked: $safety")
            println("Data dentro : $data")
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = safety,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    isBranded = data.isBranded != null && data.name != null,
                    qrCode = data.qrCode,
                )
            )
            println("Short URL created: $su")
            return su
        } else {
            throw InvalidUrlException(url)
        }
    }
}
