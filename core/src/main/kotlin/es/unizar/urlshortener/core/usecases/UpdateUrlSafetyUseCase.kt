@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

/**
 * Given an url and a [UrlSafetyResponse] updates the safety of the short URL.
 */
interface UpdateUrlSafetyUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param data The Safety data for the short URL.
     * @return The updated [ShortUrl] entity.
     */
    fun updateUrlSafety(url: String, safety: UrlSafetyResponse) : ShortUrl?
}

/**
 * Implementation of [UpdateUrlSafetyUseCase].
 */
class UpdateUrlSafetyUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
) : UpdateUrlSafetyUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param data The Safety data for the short URL.
     * @return The updated [ShortUrl] entity, or null if the URL is not found.
     */
    override fun updateUrlSafety(url: String, safety: UrlSafetyResponse) : ShortUrl? {
        return try {
            val shortUrl = safeCall { shortUrlRepository.findByKey(url) }
                ?: throw ShortUrlNotFoundException("Short URL not found for key: $url")
    
            // update safe property with the new safety data
            val updatedShortUrl = shortUrl.copy(properties = shortUrl.properties.copy(safe = safety))
            println(updatedShortUrl)
            safeCall { shortUrlRepository.save(updatedShortUrl) }
        } catch (e: ShortUrlNotFoundException) {
            println("Database hasn't been updated yet! $e.message")
            null
        }
    }
}   
