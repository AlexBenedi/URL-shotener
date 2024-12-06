@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery

import com.google.gson.Gson
import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.springbootkafkaexample.service.KafkaProducerService
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import es.unizar.urlshortener.core.usecases.*

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    /**
     * Validates the given URL.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, false otherwise
     */
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        /**
         * A URL validator that supports HTTP and HTTPS schemes.
         */
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }

}

/**
 * Implementation of the port [HashService].
 */
class HashServiceImpl : HashService {
    /**
     * Generates a hash for the given URL using the Murmur3 32-bit hashing algorithm.
     *
     * @param url the URL to hash
     * @return the hash of the URL as a string
     */
    override fun hasUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}

/**
 * Implementation of the port [SafetyService].
 */
@Service
class SafetyServiceImpl(
    private val kafkaProducerService: KafkaProducerService
) : SafetyService {
    companion object{
        /**
         * The kafka topic to check the safety of a URL.
         */
        const val CHECK_SAFETY_TOPIC = "check-safety"
    }
    /**
     * Checks if the given URL is safe.
     *
     * @param petition The petition to check the safety of a URL. 
     *        Contains the URL and an its ID.
     * @return true if the URL is safe, false otherwise
     */
    override fun isUrlSafe(petition: UrlSafetyPetition) = 
        // need to serialize the object as kafka onlin accepts strings
        kafkaProducerService.sendMessage(CHECK_SAFETY_TOPIC, Gson().toJson(petition)) 
}


/**
 * Service to check if a non-registered user can be redirected.
 */
@Service
class NonRegisteredUserService {

    private val ipRedirectionCount = ConcurrentHashMap<String, Pair<Int, Instant>>()
    companion object {
        private const val LIMIT = 5 // Limit of redirections
        private const val TIME_WINDOW = 3600L // 1 hour in seconds
    }

    fun canRedirect(ip: String): Boolean {
        val currentTime = Instant.now()
        val (count, timestamp) = ipRedirectionCount[ip] ?: Pair(0, currentTime)

        // Check if the time window has expired
        if (currentTime.epochSecond - timestamp.epochSecond > TIME_WINDOW) {
            ipRedirectionCount[ip] = Pair(1, currentTime) // Reset count
            return true
        }

        // Check if the redirection count exceeds the limit
        return if (count < LIMIT) {
            ipRedirectionCount[ip] = Pair(count + 1, timestamp)
            true
        } else {
            false
        }
    }
}

@Service
class BrandedServiceImpl(
    private val kafkaProducerService: KafkaProducerService
) : BrandedService {

    companion object{
        /**
         * The Branded links topic to check the validation of the id.
         */
        const val BRANDED_TOPIC = "branded"
    }
     /**
     * Validates if the given id can be used.
     *
     * @param id The id to be validated.
     * @return True if the id is valid, false otherwise.
     */
    override fun isValidBrandedUrl(id: String) {
        kafkaProducerService.sendMessage(BRANDED_TOPIC, id) 
    }
}

@Service
class QrServiceImpl(
    private val kafkaProducerService: KafkaProducerService
) : QrService {

    companion object{
        /**
         * The QR code topic to check the validation of the id.
         */
        const val QR_TOPIC = "qr"
    }
        /**
        * Generates a QR code for the given id.
        *
        * @param id The id to generate the QR code.
        */
    override fun generateQr(id: UrlForQr?) {
        kafkaProducerService.sendMessage(QR_TOPIC, Gson().toJson(id))
    }
}
