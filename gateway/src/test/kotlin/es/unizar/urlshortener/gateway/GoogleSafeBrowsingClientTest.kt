@file:Suppress("WildcardImport")

package es.unizar.urlshortener.gateway

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes=[GoogleSafeBrowsingClient::class])
class GoogleSafeBrowsingClientTest {
    @Autowired 
    lateinit var client: GoogleSafeBrowsingClient

    @Test
    fun `isUrlSafe returns true when API response has no matches`() {
        // Unizar should be safe 
        val result = client.isUrlSafe("https://unizar.es/")
        println(result)

        assertTrue(result.isSafe ?: false) // If null, it is not safe yet
    }

    @Test
    fun `isUrlSafe returns false when API response has matches`() {
        // This URL is a phishing test page 
        val result = client.isUrlSafe("https://testsafebrowsing.appspot.com/s/phishing.html")

        assertTrue(result.isSafe == false)
    }

    @Test
    fun `isUrlSafe provides information when URL is unsafe`(){
        // This URL is a malware test page 
        val result = client.isUrlSafe("https://testsafebrowsing.appspot.com/s/malware.html")

        assertTrue(result.threatType == "MALWARE")
        assertTrue(result.platformType == "ANY_PLATFORM")
        assertTrue(result.threatEntryType == "URL")
        assertTrue(result.threatInfo == "https://testsafebrowsing.appspot.com/s/malware.html")
    }
}
