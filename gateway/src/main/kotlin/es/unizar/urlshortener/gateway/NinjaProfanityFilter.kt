package es.unizar.urlshortener.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException

@Component
class NinjaProfanityFilter(
    private val restTemplate: RestTemplate? = null
) {
    @Value("\${ninja.profanity.filter.api.key}")
    private lateinit var apiKey: String

    /**
     * Checks if a given name contains profanity using the API Ninja Profanity Filter.
     *
     * @param name The name to be checked.
     * @return Boolean indicating if the name is valid (does not contain profanity).
     */
    fun isNameValid(name: String): Boolean {
        val effectiveRestTemplate = restTemplate ?: RestTemplate()
        val url = "https://api.api-ninjas.com/v1/profanityfilter?text=$name"
        println("URL: $url")

        val headers = HttpHeaders().apply {
            set("Content-Type", "application/json")
            set("X-Api-Key", apiKey)
        }

        val entity = HttpEntity<String>(headers)

        return try {
            // Send the request to the Ninja Profanity Filter API
            val response: ResponseEntity<ResponseNinja> = effectiveRestTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ResponseNinja::class.java
            )
            println("Ninja response: ${response.body?.original} -> ${response.body?.has_profanity}")
            !(response.body?.has_profanity ?: true)
        } catch (e: HttpClientErrorException) {
            println("HTTP error: ${e.statusCode} - ${e.responseBodyAsString}")
            false // Treat as invalid if the API fails
        } catch (e: RestClientException) {
            println("Client error: ${e.message}")
            false
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
            false
        }
    }
}

/**
 * Class to model the response from the API Ninja Profanity Filter.
 */
class ResponseNinja(
    val original: String? = null,
    val censored: String? = null,
    val has_profanity: Boolean? = null
)
