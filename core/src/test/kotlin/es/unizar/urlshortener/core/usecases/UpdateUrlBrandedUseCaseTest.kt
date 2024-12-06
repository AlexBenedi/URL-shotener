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

    @Test
    fun `updateUrlvalidBranded updates the database with the new url state info`() {
        val repository = mock<ShortUrlRepositoryService>()
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl("key", redirection, properties = ShortUrlProperties(validBranded = null, isBranded = true))
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = UpdateUrlBrandedUseCaseImpl(repository) // Cambiado a la implementación
        whenever(repository.save(any())).doAnswer { it.arguments[0] as ShortUrl }
        val updatedUrl = useCase.updateUrlBranded("key", true)
        println(updatedUrl)
        assertEquals(true, updatedUrl?.properties?.validBranded)
    }

    @Test
    fun `updateUrlBranded returns ShortUrlNotFoundException if the url is not found`(){
        val repository = mock<ShortUrlRepositoryService>()
        whenever(repository.findByKey("key")).thenReturn(null)
        val useCase = UpdateUrlBrandedUseCaseImpl(repository) // Cambiado a la implementación
        val updatedUrl = useCase.updateUrlBranded("key", true)
        assertEquals(null, updatedUrl)
    }

}

