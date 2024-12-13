package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ShortUrlNotFoundException
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateUrlBrandedUseCaseTest {

    /**
     * Tests that `updateUrlBranded` updates the database with the new URL state information.
     *
     * Mocks the repository, redirection, and short URL, and verifies that the URL 
     * state information is updated correctly.
     */
    @Test
    fun `updateUrlvalidBranded updates the database with the new url state info`() {
        val repository = mock<ShortUrlRepositoryService>()
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl(
            "key", 
            redirection, 
            properties = ShortUrlProperties(validBranded = null, isBranded = true))

        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = UpdateUrlBrandedUseCaseImpl(repository) // Cambiado a la implementación
        whenever(repository.save(any())).doAnswer { it.arguments[0] as ShortUrl }
        val updatedUrl = useCase.updateUrlBranded("key", true)
        println(updatedUrl)
        assertEquals(true, updatedUrl?.properties?.validBranded)
    }

    /**
     * Tests that `updateUrlBranded` returns `ShortUrlNotFoundException` if the URL is not found.
     *
     * Mocks the repository and verifies that the method returns null when the URL is not found.
     */
    @Test
    fun `updateUrlBranded returns ShortUrlNotFoundException if the url is not found`(){
        val repository = mock<ShortUrlRepositoryService>()
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = UpdateUrlBrandedUseCaseImpl(repository) // Cambiado a la implementación
        val updatedUrl = useCase.updateUrlBranded("key", true)
        assertEquals(null, updatedUrl)
    }

}

