@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*


/**
 * Given an url and a [QR] updates the state of the short URL.
 */
interface StoreQRUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param qrCode The QR code of the short URL.
     * @return The updated [ShortUrl] entity.
     */
    fun storeQR(url: String, qrCode: String) : ShortUrl?
}


/**
 * Implementation of [UpdateUrlBrandedUseCase].
 */
class StoreQRUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
) : StoreQRUseCase {
    /**
     * Updates the safety of a short URL.
     *
     * @param url The URL to be updated.
     * @param qrCode The QR code of the short URL.
     * @return The updated [ShortUrl] entity, or null if the URL is not found.
     */
    override fun storeQR(url: String, qrCode: String) : ShortUrl? {
        return try {
            val shortUrl = safeCall { shortUrlRepository.findByKey(url) }
                ?: throw ShortUrlNotFoundException("Short URL not found for key: $url")

            val updatedShortUrl = shortUrl.copy(qrCode = qrCode)
            println("Qr code guardado $updatedShortUrl")
            safeCall { shortUrlRepository.save(updatedShortUrl) }
        } catch (e: ShortUrlNotFoundException) {
            println("Database hasn't been updated yet! $e.message")
            null
        }
    }
}
