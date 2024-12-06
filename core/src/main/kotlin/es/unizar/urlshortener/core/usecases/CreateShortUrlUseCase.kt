@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

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
    private val brandedService: BrandedService,
    private val qrService: QrService
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

            if (data.generateQrCode == true) {
                qrService.generateQr(id)
            }

            if (data.isBranded == true ) {
                if ( data.name != null ) {
                    //brandedService.isValidBrandedUrl(id)
                    id = data.name
                } else {
                    throw InvalidNameBrandedUrl()
                }
            }

            safeCall { safetyService.isUrlSafe(UrlSafetyPetition(url, id)) } // post kafka message
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = null,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    isBranded = data.isBranded != null && data.name != null,
                    generateQrCode = data.generateQrCode
                )
            )
            val short = safeCall { shortUrlRepository.save(su) }
            if (data.isBranded == true ) {
                if ( data.name != null ) {
                    brandedService.isValidBrandedUrl(id)
                } else {
                    throw InvalidNameBrandedUrl()
                }
            }
            return short
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

            if (data.generateQrCode == true) {
                qrService.generateQr(id)
            }

            if (data.isBranded == true ) {
                if ( data.name != null ) {
                    id = data.name
                } else {
                    throw InvalidNameBrandedUrl()
                }
            }
            safeCall { safetyService.isUrlSafe(UrlSafetyPetition(url, id)) }// this must be async
            println("Data dentro : $data")
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = null,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    isBranded = data.isBranded != null && data.name != null,
                    generateQrCode = data.generateQrCode
                )
            )
            println("Short URL created: $su")
            return su
        } else {
            throw InvalidUrlException(url)
        }
    }
}
