package es.unizar.urlshortener.infrastructure.delivery

import com.google.gson.Gson
import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.SafetyService
import es.unizar.urlshortener.core.UrlSafetyPetition
import es.unizar.urlshortener.springbootkafkaexample.service.KafkaProducerService
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

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
