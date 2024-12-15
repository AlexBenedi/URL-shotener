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

    /**
     * Tests that `storeQR` updates the database with the new QR code.
     *
     * Mocks the repository, redirection, and short URL, and verifies that the QR code information is updated correctly.
     */
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

    /**
     * Tests that `storeQR` throws an exception when the short URL is not found.
     *
     * Mocks the repository and verifies that an exception is thrown when the short URL is not found.
     */
    @Test
    fun `StoreQRUse throws an exception when the short URL is not found`() {
        val repository = mock<ShortUrlRepositoryService>()
        whenever(repository.findByKey("1111")).thenReturn(null)
        val useCase = StoreQRUseCaseImpl(repository)
        assertFailsWith<ShortUrlNotFoundException> {
            useCase.storeQR("1111", "esto es un qr")
        }
    }
}

