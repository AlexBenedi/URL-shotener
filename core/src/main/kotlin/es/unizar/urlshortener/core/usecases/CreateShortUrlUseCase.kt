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

    fun createAndDoNotSave(url: String, data: ShortUrlProperties, userId : String, isSyncMode : Boolean): ShortUrl

    fun findByKey(key: String): ShortUrl?

    fun save(shortUrl: ShortUrl): ShortUrl
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
        if (safeCall { validatorService.isValid(url) }) {
            var id = safeCall { hashService.hasUrl(url) }


            if (data.isBranded == true) {
                id = data.name ?: throw EmptyNameBrandedUrl()
            }

            val idName = if(data.ip == null){
                            "1.1.1.1"
                            }
                            else{
                                data.ip
                            }
            if (data.generateQrCode == true) {
                qrService.generateQr(UrlForQr(url, id, idName))
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
            println("Short URL created: $short")
            if (data.isBranded == true ) {
                brandedService.isValidBrandedUrl(id)
            }
            return short
        } else {
            throw InvalidUrlException(url)
        }
    }

    override fun createAndDoNotSave(url: String, data: ShortUrlProperties, userId : String, isSyncMode : Boolean):
            ShortUrl{
        // Get the user ID from the data (modify as needed to get the actual user ID)

        if (safeCall { validatorService.isValid(url) }) {
            /*if (!safeCall { safetyService.isUrlSafe(url) }) {
                println("URL is not safe")
                throw UnsafeUrlException(url)
            }*/
            var id = safeCall { hashService.hasUrl(url) }
            id += userId
            System.out.println("ID de la url a insertar: " + id)

            if (data.isBranded == true) {
                id = data.name ?: throw EmptyNameBrandedUrl()
            }

            if (data.generateQrCode == true) {
                qrService.generateQr(UrlForQr(url, id, userId))
            }
            // Si es sincrono, se llama a la funcion sincrona, si no, se llama a la asincrona
            if(!isSyncMode){
                safeCall { safetyService.isUrlSafe(UrlSafetyPetition(url, id)) }// this must be async
            }
            else{
                safeCall { safetyService.isUrlSafeSync(url); }// this must be async
            }

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

    /**
     * Finds a short URL by its key.
     */
    override fun findByKey(key: String): ShortUrl? {
        return safeCall { shortUrlRepository.findByKey(key) }
    }

    /**
     * Saves a short URL in the repository.
     */
    override fun save(shortUrl: ShortUrl): ShortUrl {
        return safeCall { shortUrlRepository.save(shortUrl) }
    }
}
