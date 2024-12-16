package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.safeCall
import es.unizar.urlshortener.core.UnsafeUrlException
import es.unizar.urlshortener.core.UrlSafetyNotCheckedException
import es.unizar.urlshortener.core.BrandedNotCheckedException
import es.unizar.urlshortener.core.InvalidNameBrandedUrl

/**
 * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
 * and an [HTTP redirection mode][Redirection.mode].
 *
 * **Note**: This is an example of functionality.
 */
interface RedirectUseCase {
    /**
     * Redirects to the target URL associated with the given key.
     *
     * @param key The key associated with the target URL.
     * @return The [Redirection] containing the target URL and redirection mode.
     * @throws RedirectionNotFound if no redirection is found for the given key.
     */
    fun redirectTo(key: String): Redirection
}

/**
 * Implementation of [RedirectUseCase].
 */
class RedirectUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService
) : RedirectUseCase {
    /**
     * Redirects to the target URL associated with the given key.
     *
     * @param key The key associated with the target URL.
     * @return The [Redirection] containing the target URL and redirection mode.
     * @throws RedirectionNotFound if no redirection is found for the given key, 
     *         UrlSafetyNotCheckedException if URL's safety hasn't been checked yet
     *         and UnsafeUrlException if URL is unsafe. 
     */
    override fun redirectTo(key: String) : Redirection {
        val shortUrl = safeCall { shortUrlRepository.findByKey(key) }
        if(shortUrl != null){
            val safetyResponse = shortUrl.properties.safe
            val isBranded = shortUrl.properties.isBranded
            val validBranded = shortUrl.properties.validBranded
            if(safetyResponse == null || safetyResponse.isSafe == null){ // safety not checked yet
                throw UrlSafetyNotCheckedException()
            } 
            else if (safetyResponse.isSafe == false) { // url is unsafe
                throw UnsafeUrlException(
                    safetyResponse.threatType ?: "Unknown",
                    safetyResponse.platformType ?: "Unknown",
                    safetyResponse.threatEntryType ?: "Unknown",
                    safetyResponse.threatInfo ?: "Unknown"
                )
            }
            else if  (isBranded == true && validBranded == null) {
                throw BrandedNotCheckedException()
            }
            else if (isBranded == true && validBranded == false) {
                throw InvalidNameBrandedUrl(key)
            }

            return shortUrl.redirection
        } else {
            throw RedirectionNotFound(key)
        }
    }
}
