package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.SafetyService
import es.unizar.urlshortener.gateway.GoogleSafeBrowsingClient
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

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

    /**
     * Validates if the given id can be used.
     *
     * @param id The id to be validated.
     * @return True if the id is valid, false otherwise.
     */
    override fun isValidBrandedUrl(id: String?): Boolean = id != null
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
    private val googleSafeBrowsingClient: GoogleSafeBrowsingClient
) : SafetyService {
    /**
     * Checks if the given URL is safe.
     *
     * @param url the URL to check
     * @return true if the URL is safe, false otherwise
     */
    override fun isUrlSafe(url: String) = googleSafeBrowsingClient.isUrlSafe(url) 
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
