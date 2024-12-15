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

@SpringBootTest(classes=[NinjaProfanityFilter::class])
class NinjaProfanityFilterTest {
    @Autowired 
    lateinit var ninja: NinjaProfanityFilter

    /**
     * Tests that `isNameValid` returns true when the API response has no profanity.
     */
    @Test
    fun `isNameValid returns true when API response has no profanity`() { 
        val result = ninja.isNameValid("Unizar")
        println(result)

        assertTrue(result == true) // If null, it is not safe yet.
    }

    /**
     * Tests that `isNameValid` returns false when the API response has profanity.
     */
    @Test
    fun `isNameValid returns false when API response has profanity`() {
        val result = ninja.isNameValid("Puta")
        println(result)

        assertTrue(result == false)
    }
}
