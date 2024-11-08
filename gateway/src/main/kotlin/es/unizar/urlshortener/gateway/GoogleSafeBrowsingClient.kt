package es.unizar.urlshortener.gateway

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.http.ResponseEntity

@Component
class GoogleSafeBrowsingClient(
    private val restTemplate: RestTemplate? = null
) {
    private val apiKey = System.getenv("GOOGLE_API_SAFETY_KEY") ?: ""
    private val url = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey"

    /**
     * Checks if a given URL is safe by querying the Google Safe Browsing API.
     *
     * @param targetUrl The URL to be checked for safety.
     * @return UrlSafetyResponse containing the safety status and threat details if any.
     */
    fun isUrlSafe(targetUrl: String): UrlSafetyResponse {
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
            * We add the unsafety information if it is not. 
            */
            val isSafe = response.body?.matches?.isEmpty() ?: true
            UrlSafetyResponse(
                isSafe = isSafe,
                threatType = response.body?.matches?.firstOrNull()?.threatType,
                platformType = response.body?.matches?.firstOrNull()?.platformType,
                threatEntryType = response.body?.matches?.firstOrNull()?.threatEntryType,
                threatInfo = response.body?.matches?.firstOrNull()?.threat?.url,
            )
        } catch (e: HttpClientErrorException) {
            println(e.message)
            UrlSafetyResponse(
                isSafe = false, // or maybe null?
                threatType = "Verification error",
            )
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

// Represents the information about the URL which will be returned 
data class UrlSafetyResponse(
    val isSafe: Boolean, 
    val threatType: String? = null, 
    val platformType: String? = null, 
    val threatEntryType: String? = null, 
    val threatInfo: String? = null
)