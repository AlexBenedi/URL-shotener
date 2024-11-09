package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.UnsafeUrlException
import es.unizar.urlshortener.core.SafetyService
import es.unizar.urlshortener.core.InvalidNameBrandedUrl
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ValidatorService
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.LimitExceededException

class CreateShortUrlUseCaseTest {

    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
    

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
            )
        val shortUrl = createShortUrlUseCase.create("http://example.com/", shortUrlProperties)

        assertEquals(shortUrl.hash, "f684a3c4")
    }

    @Test
    fun `creates returns invalid URL exception if the URL is not valid`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val safetyService = mock<SafetyService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("ftp://example.com/")).thenReturn(false)
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService, 
                safetyService,
            )

        assertFailsWith<InvalidUrlException> {
            createShortUrlUseCase.create("ftp://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the URI cannot be validated`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val safetyService = mock<SafetyService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenThrow(RuntimeException())
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService, 
                safetyService,
                )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the hash cannot be computed`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val safetyService = mock<SafetyService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenThrow(RuntimeException())
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(
            shortUrlRepository, 
            validatorService, 
            hashService, 
            safetyService, 
            )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the short URL cannot be saved`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val safetyService = mock<SafetyService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).thenThrow(RuntimeException())
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService, 
                safetyService,
                )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }
    @Ignore("This test is not needed now TODO: Readapt to the new implementation") 
    @Test
    fun `creates returns unsafe URL exception if the URL is not safe`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val safetyService = mock<SafetyService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        // Mock the safety service to return false
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(false))

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService, 
                safetyService,
                )

        assertFailsWith<UnsafeUrlException> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test 
    fun `creates returns a valid branded short URL`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
        = CreateShortUrlUseCaseImpl(
            shortUrlRepository, 
            validatorService, 
            hashService,
            safetyService,
        )
        val properties = ShortUrlProperties(isBranded = true, name = "branded")
        val shortUrl = createShortUrlUseCase.create("http://example.com/", properties)

        assertEquals(shortUrl.hash, "branded")
    }

    @Test
    fun `create a return invalid branded link exception if name is empty`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
            )
        assertFailsWith<InvalidNameBrandedUrl> {
            createShortUrlUseCase.create("http://example.com/", ShortUrlProperties(isBranded = true))
        }
    }

    @Test
    fun `creates returns a basic redirect if it can compute a hash and branded flag is desactivated`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(safetyService.isUrlSafe("http://example.com/")).thenReturn(UrlSafetyResponse(true))
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
            )
        val shortUrl = createShortUrlUseCase.create("http://example.com/", ShortUrlProperties(isBranded = false))

        assertEquals(shortUrl.hash, "f684a3c4")
    }

    // New test to simulate user exceeding the limit
    @Test
    fun `throws LimitExceededException when user exceeds the limit of shortened URLs`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = ShortUrlProperties(ip = "127.0.0.1", sponsor = "user123")
        val safetyService = mock<SafetyService>()

        // Simulate that the user has already shortened 5 URLs
        whenever(shortUrlRepository.countShortenedUrlsByUser("user123")).thenReturn(5)

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(
                                                        shortUrlRepository, 
                                                        validatorService, 
                                                        hashService, 
                                                        safetyService,
                                                    )

        // Expect a LimitExceededException to be thrown
        assertFailsWith<LimitExceededException> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }
}
