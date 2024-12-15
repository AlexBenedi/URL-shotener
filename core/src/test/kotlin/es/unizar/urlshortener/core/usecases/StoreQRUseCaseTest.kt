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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


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
     * Tests that `storeQR` throws an exception when the URL is not found.
     *
     * Mocks the repository and verifies that the method throws a `ShortUrlNotFoundException`
     * when the URL is not found.
     */
    @Test
    fun `storeQR returns null when URL not found`() {
        val repository = mock<ShortUrlRepositoryService>()

        whenever(repository.findByKey("key")).thenReturn(null)

        val useCase = StoreQRUseCaseImpl(repository)
        val qr = "esto es un qr mas"
        val updatedUrl = useCase.storeQR("key", qr)

        assertNull(updatedUrl)
        verify(repository).findByKey("key")
        verify(repository, never()).save(any())
    }

    /**
     * Tests that `storeQR` handles an exception when saving the updated URL.
     *
     * Mocks the repository, redirection, and short URL, and verifies that the
     * method handles an exception when saving the updated URL.
     */
    @Test
    fun `storeQR handles exception when saving updated URL`() {
        val repository = mock<ShortUrlRepositoryService>()
        val redirection = mock<Redirection>()
        val shortUrl = ShortUrl(
            "key", 
            redirection, 
            properties = ShortUrlProperties(validBranded = null, isBranded = true)
        )

        whenever(repository.findByKey("key")).thenReturn(shortUrl)
        whenever(repository.save(any())).thenThrow(RuntimeException("Unexpected error"))

        val useCase = StoreQRUseCaseImpl(repository)
        val qr = "esto es un qr mas"

        val exception = assertFailsWith<RuntimeException> {
            useCase.storeQR("key", qr)
        }

        assertEquals("Unexpected error", exception.message)
        verify(repository).findByKey("key")
        verify(repository).save(any())
    }
}

