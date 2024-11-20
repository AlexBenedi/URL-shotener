package es.unizar.urlshortener.core.usecases

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.QRCode
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlNotFoundException
import java.io.ByteArrayOutputStream
import java.util.Base64
import org.slf4j.LoggerFactory
import java.net.URISyntaxException


private val logger = LoggerFactory.getLogger("GenerateQRCodeUseCase")

/**
 * Interface to generate a QR code for a given URL.
 */
interface GenerateQRCodeUseCase {
    /**
     * Generates a QR code for the given URL.
     *
     * @param url The URL to generate a QR code for.
     * @param size The size of the QR code (default is 250x250).
     * @return A [QRCode] data class containing the URL, the QR code image in Base64, and the size.
     */
    fun generateQRCode(url: String, hash: String, size: Int = 250): QRCode
}

/**
 * Implementation of [GenerateQRCodeUseCase].
 */
class GenerateQRCodeUseCaseImpl(
    private val shortUrlRepositoryService: ShortUrlRepositoryService // Use the port here
): GenerateQRCodeUseCase {
    override fun generateQRCode(url: String, hash: String, size: Int): QRCode {
        if (!isValidUrl(url)) {
            throw InvalidUrlException(url)
        }
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size)
        ByteArrayOutputStream().use { outputStream ->
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            val qrCodeBytes = outputStream.toByteArray()
            val base64Image = Base64.getEncoder().encodeToString(qrCodeBytes)

            // Find the ShortUrl in the repository
            val shortUrl = shortUrlRepositoryService.findByKey(hash)
                ?: throw UrlNotFoundException(url) // Throw exception if the URL is not found

            // Update the ShortUrlProperties with the generated QR code
            val updatedProperties = shortUrl.properties.copy(qrCode = base64Image)

            // Create an updated ShortUrl with the new properties
            val updatedShortUrl = shortUrl.copy(properties = updatedProperties)

            // Save the updated ShortUrl
            shortUrlRepositoryService.save(updatedShortUrl)

            return QRCode(
                url = url,
                base64Image = base64Image,
                size = size
            )
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            uri.scheme != null && uri.host != null
        } catch (e: URISyntaxException) {
            logger.warn("Invalid URL: $url", e)
            false
        }
    }
}
