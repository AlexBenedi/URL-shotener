package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.ShortUrlNotFoundException
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateUrlSafetyUseCaseTest {

    /**
     * Tests that `updateUrlSafety` updates the database with the new URL safety information.
     *
     * Mocks the repository, redirection, and safety response, and verifies that the URL 
     * safety information is updated correctly.
     */
    @Test
    fun `updateUrlSafety updates the database with the new url safety info`() {
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        val shortUrl = ShortUrl("key", redirection, properties = ShortUrlProperties(safe = null))
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = UpdateUrlSafetyUseCaseImpl(repository)
        whenever(repository.save(any())).doAnswer { it.arguments[0] as ShortUrl }
        println(safety.isSafe)
        val updatedUrl = useCase.updateUrlSafety("key", safety)
        println(updatedUrl)

        assertEquals(safety, updatedUrl?.properties?.safe)
    }

    /**
     * Tests that `updateUrlSafety` returns `ShortUrlNotFoundException` if the URL is not found.
     *
     * Mocks the repository and safety response, and verifies that the method returns null when the URL is not found.
     */
    @Test
    fun `updateUrlSafety returns ShortUrlNotFoundException if the url is not found`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = UpdateUrlSafetyUseCaseImpl(repository)

        val updatedUrl = useCase.updateUrlSafety("key", safety)

        assertEquals(null, updatedUrl)
    }

}

