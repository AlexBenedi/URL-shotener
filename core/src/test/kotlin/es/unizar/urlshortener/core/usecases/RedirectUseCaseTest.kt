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
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RedirectUseCaseTest {

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

    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        val repository = mock<ShortUrlRepositoryService> ()
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<RedirectionNotFound> {
            useCase.redirectTo("key")
        }
    }

    @Test
    fun `redirectTo returns a not found when find by key fails`() {
        val repository = mock<ShortUrlRepositoryService> ()
        whenever(repository.findByKey("key")).thenThrow(RuntimeException())
        val useCase = RedirectUseCaseImpl(repository)

        assertFailsWith<InternalError> {
            useCase.redirectTo("key")
        }
    }

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
}

