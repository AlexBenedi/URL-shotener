@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*


/**
 * Given an url and a [BrandedState] updates the state of the short Branded URL.
 */
interface UpdateUrlBrandedUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param isValid State of the short URL.
     * @return The updated [ShortUrl] entity.
     */
    fun updateUrlBranded(url: String, isValid: Boolean) : ShortUrl?
}


/**
 * Implementation of [UpdateUrlBrandedUseCase].
 */
class UpdateUrlBrandedUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
) : UpdateUrlBrandedUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param isValid State of the short URL.
     * @return The updated [ShortUrl] entity, or null if the URL is not found.
     */
    override fun updateUrlBranded(url: String, isValid: Boolean) : ShortUrl? {
        return try {
            val shortUrl = safeCall { shortUrlRepository.findByKey(url) }
                ?: throw ShortUrlNotFoundException("Short URL not found for key: $url")
            
            // update validBranded property with the state
            val updatedShortUrl = shortUrl.copy(properties = shortUrl.properties.copy(validBranded = isValid))
            safeCall { shortUrlRepository.save(updatedShortUrl) }
        } catch (e: ShortUrlNotFoundException) {
            println("Database hasn't been updated yet! $e.message")
            null
        }
    }
}   
