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

    @Test
    fun `updateUrlSafety returns ShortUrlNotFoundException if the url is not found`(){
        val repository = mock<ShortUrlRepositoryService> ()
        val safety = mock<UrlSafetyResponse>()
        whenever(safety.isSafe).thenReturn(true)
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = UpdateUrlSafetyUseCaseImpl(repository)

        assertFailsWith<ShortUrlNotFoundException> {
            useCase.updateUrlSafety("key", safety)
        }
    }

}

