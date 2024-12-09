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

class StoreQRUseCaseTest {

    @Test
    fun `StoreQRUse updates the database with the new qr`() {
        val repository = mock<ShortUrlRepositoryService>()
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl(
            "key", 
            redirection, 
            properties = ShortUrlProperties(validBranded = null, isBranded = true))
        
        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        val useCase = StoreQRUseCaseImpl(repository)
        whenever(repository.save(any())).doAnswer { it.arguments[0] as ShortUrl }
        val qr = "esto es un qr mas"
        val updatedUrl = useCase.storeQR("key", qr)
        println(updatedUrl)
        assertEquals(qr, updatedUrl?.qrCode)
    }
}

