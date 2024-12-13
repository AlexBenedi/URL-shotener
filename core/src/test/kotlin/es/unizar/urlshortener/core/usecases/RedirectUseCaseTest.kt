package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.UrlSafetyNotCheckedException
import es.unizar.urlshortener.core.UnsafeUrlException
import es.unizar.urlshortener.core.BrandedNotCheckedException
import es.unizar.urlshortener.core.InvalidNameBrandedUrl
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RedirectUseCaseTest {


    /**
     * Tests that `redirectTo` returns a redirect when the key exists.
     *
     * Mocks the repository, redirection, and URL safety response, and 
     * verifies that the redirection is returned correctly.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        val shortUrl = ShortUrl("key", redirection, properties = ShortUrlProperties(safe = safety))
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(repository)

        assertEquals(redirection, useCase.redirectTo("key"))
    }


    /**
     * Tests that `redirectTo` returns a not found exception when the key does not exist.
     *
     * Mocks the repository and verifies that the method throws `RedirectionNotFound` when the key is not found.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        val repository = mock<ShortUrlRepositoryService> ()
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<RedirectionNotFound> {
            useCase.redirectTo("key")
        }
    }

    /**
     * Tests that `redirectTo` returns an internal error when find by key fails.
     *
     * Mocks the repository and verifies that the method throws `InternalError` 
     * when the repository throws an exception.
     */
    @Test
    fun `redirectTo returns a not found when find by key fails`() {
        val repository = mock<ShortUrlRepositoryService> ()
        whenever(repository.findByKey("key")).thenThrow(RuntimeException())
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<InternalError> {
            useCase.redirectTo("key")
        }
    }

    /**
     * Tests that `redirectTo` returns a URL safety not checked exception when URL safety is not checked.
     *
     * Mocks the repository, redirection, and URL properties, and verifies that the method throws 
     * `UrlSafetyNotCheckedException` when URL safety is null.
     */
    @Test
    fun `redirectTo returns url not checked when url safety is not checked`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val properties = mock<ShortUrlProperties>()
        whenever(properties.safe).thenReturn(null) // safety is null
        val shortUrl = ShortUrl("key", redirection, properties = properties)
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<UrlSafetyNotCheckedException> {
            useCase.redirectTo("key")
        }
    } 

    /**
     * Tests that `redirectTo` returns an unsafe URL exception when URL is not safe.
     *
     * Mocks the repository, redirection, and URL safety response, and verifies that the 
     * method throws `UnsafeUrlException` when URL safety is false.
     */
    @Test
    fun `redirectTo returns unsafe url when url is not safe`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(false) // mock url unsafety
        val shortUrl = ShortUrl("key", redirection, properties = ShortUrlProperties(safe = safety))
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<UnsafeUrlException> {
            useCase.redirectTo("key")
        }
    }

    /**
     * Tests that `redirectTo` returns a branded not checked exception when URL is branded and not checked.
     *
     * Mocks the repository, redirection, URL properties, and URL safety response, and verifies that the 
     * method throws `BrandedNotCheckedException` when branded URL validity is null.
     */
    @Test
    fun `redirectTo returns branded not checked when url is branded and not checked`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val properties = mock<ShortUrlProperties>()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        whenever(properties.isBranded).thenReturn(true)
        whenever(properties.validBranded).thenReturn(null)
        whenever(properties.safe).thenReturn(safety)
        val shortUrl = ShortUrl("key", redirection, properties = properties)
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<BrandedNotCheckedException> {
            useCase.redirectTo("key")
        }
    }

    /**
     * Tests that `redirectTo` returns a branded invalid exception when URL is branded, checked, and invalid.
     *
     * Mocks the repository, redirection, URL properties, and URL safety response, and verifies that the 
     * method throws `InvalidNameBrandedUrl` when branded URL validity is false.
     */
    @Test
    fun `redirectTo returns branded invalid when url is branded is checked and invalid`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val redirection = mock<Redirection>()
        val properties = mock<ShortUrlProperties>()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        whenever(properties.isBranded).thenReturn(true)
        whenever(properties.validBranded).thenReturn(false)
        whenever(properties.safe).thenReturn(safety)
        val shortUrl = ShortUrl("key", redirection, properties = properties)
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<InvalidNameBrandedUrl> {
            useCase.redirectTo("key")
        }
    }
}

