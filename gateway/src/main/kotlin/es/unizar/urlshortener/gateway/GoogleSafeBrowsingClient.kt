package es.unizar.urlshortener.gateway

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.http.ResponseEntity

@Component
class GoogleSafeBrowsingClient(
    private val restTemplate: RestTemplate? = null
) {
    private val apiKey = System.getenv("GOOGLE_SAFE_BROWSING_API_KEY") ?: ""
    private val url = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey"

    fun isUrlSafe(targetUrl: String): Boolean {
        // Request body following the Google Safe Browsing API
        val effectiveRestTemplate = restTemplate ?: RestTemplate()
        val requestBody = mapOf(
            "client" to mapOf(
                "clientId" to "urlshortener-fractal-link",
                "clientVersion" to "1.0"
            ),
            "threatInfo" to mapOf(
                "threatTypes" to listOf(
                    "MALWARE", 
                    "SOCIAL_ENGINEERING", 
                    "UNWANTED_SOFTWARE", 
                    "POTENTIALLY_HARMFUL_APPLICATION"
                ),
                "platformTypes" to listOf("ANY_PLATFORM"),
                "threatEntryTypes" to listOf("URL"),
                "threatEntries" to listOf(mapOf("url" to targetUrl))
            )
        )

        return try {
            // Send the request to the Google Safe Browsing API, using POST as they say
            val response: ResponseEntity<GoogleSafeBrowsingResponse> = 
                effectiveRestTemplate.postForEntity(url, requestBody, GoogleSafeBrowsingResponse::class.java)

            /*
            * If the response body is empty, the URL is safe, otherwise it is not 
            * -> Maybe give more feedback in the future and not return just true or false (why is it not safe...)
            */
            response.body?.matches?.isEmpty() ?: true
        } catch (e: HttpClientErrorException) {
            println(e.message)
            false // fails --> not safe (for now, we should give more feedback)
        }
    }
}

// Google Safe Browsing API response, following the API documentation
class GoogleSafeBrowsingResponse(
    val matches: List<ThreatMatch>? = null
)

class ThreatMatch(
    val threatType: String? = null,
    val platformType: String? = null,
    val threatEntryType: String? = null,
    val threat: ThreatInfo? = null,
    val threatEntryMetadata: ThreatEntryMetadata? = null,
    val cacheDuration: String? = null
)

class ThreatInfo(
    val url: String? = null
)

class ThreatEntryMetadata(
    val entries: List<Entry>? = null
)

class Entry(
    val key: String? = null,
    val value: String? = null
)
