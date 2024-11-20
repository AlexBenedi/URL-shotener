package es.unizar.urlshortener.core

import java.time.OffsetDateTime
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.io.IOException

private const val QR_CODE_WIDTH = 200
private const val QR_CODE_HEIGHT = 200

/**
 * A [Click] captures a request of redirection of a [ShortUrl] identified by its [hash].
 */
data class Click(
    val hash: String,
    val properties: ClickProperties = ClickProperties(),
    val created: OffsetDateTime = OffsetDateTime.now(),
    val clicks : Int
)

/**
 * A [ShortUrl] is the mapping between a remote url identified by [redirection]
 * and a local short url identified by [hash].
 */
data class ShortUrl(
    val hash: String,
    val redirection: Redirection,
    val created: OffsetDateTime = OffsetDateTime.now(),
    val properties: ShortUrlProperties = ShortUrlProperties(),
    //var qrCode: String  // Base64 representation of the QR code
) {
    /**
     * Generates a QR code image for this ShortUrl and saves it to the specified output path.
     *
     * @param outputFilePath the path where the QR code image will be saved
     */
    fun generateQRCode(outputFilePath: String) {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(redirection.target, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT)

        val outputPath: Path = Paths.get(outputFilePath)
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath)
    }
}

/**
 * A [Redirection] specifies the [target] and the [status code][mode] of a redirection.
 * By default, the [status code][mode] is 307 TEMPORARY REDIRECT.
 */
data class Redirection(
    val target: String,
    val mode: Int = 307
)

/**
 * A [ShortUrlProperties] is the bag of properties that a [ShortUrl] may have.
 */
data class ShortUrlProperties(
    val ip: String? = null,
    val sponsor: String? = null,
    val safe: UrlSafetyResponse? = null,
    val owner: String? = null,
    val country: String? = null,
    val isBranded: Boolean? = null,
    val name: String? = null,
    val qrCode: String? = null
)

/**
 * A [ClickProperties] is the bag of properties that a [Click] may have.
 */
data class ClickProperties(
    val ip: String? = null,
    val referrer: String? = null,
    val browser: String? = null,
    val platform: String? = null,
    val country: String? = null
)

/**
 * A [Link] is the association between a [Click] and a [ShortUrl].
 */
data class Link(
    val click: Click,
    val shortUrl: ShortUrl,
    val userId: String
)

/**
 * A [User] is the entity that represents a user of the application.
 */
data class User(
    val userId : String
)
/**
 * A [QRCode] represents a generated QR code for a given URL.
 *
 * @param url The URL the QR code represents.
 * @param base64Image The QR code image encoded as a Base64 string.
 * @param size The size of the QR code (e.g., 250x250 pixels).
 */
data class QRCode(
    val url: String,           // The URL for which the QR code is generated
    val base64Image: String,  // The QR code image encoded in Base64 format
    val size: Int             // The size of the QR code (e.g., 250x250 pixels)
)

/**
 * Represents the safety status of a URL as determined by the Google Safe Browsing API.
 *
 * @property isSafe Indicates whether the URL is considered safe.
 * @property threatType The type of threat detected, if any (e.g., MALWARE, SOCIAL_ENGINEERING).
 * @property platformType The platform type on which the threat was detected (e.g., ANY_PLATFORM).
 * @property threatEntryType The type of threat entry (e.g., URL).
 * @property threatInfo Additional information about the threat, such as the URL.
 */
data class UrlSafetyResponse(
    val isSafe: Boolean? = null, 
    val threatType: String? = null, 
    val platformType: String? = null, 
    val threatEntryType: String? = null, 
    val threatInfo: String? = null
)
