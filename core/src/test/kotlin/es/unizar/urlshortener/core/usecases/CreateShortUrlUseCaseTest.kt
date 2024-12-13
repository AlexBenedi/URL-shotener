package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.UnsafeUrlException
import es.unizar.urlshortener.core.SafetyService
import es.unizar.urlshortener.core.InvalidNameBrandedUrl
import es.unizar.urlshortener.core.EmptyNameBrandedUrl
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.BrandedService
import es.unizar.urlshortener.core.QrService
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
    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method returns a basic redirect
     * if it can compute a hash for the given URL.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`, 
     *    `SafetyService`, `ShortUrlProperties`, `BrandedService`, and `QrService`.
     * 2. Set up the mocks to return expected values:
     *    - `validatorService.isValid("http://example.com/")` returns `true`.
     *    - `hashService.hasUrl("http://example.com/")` returns `"f684a3c4"`.
     *    - `shortUrlRepository.save(any())` returns the argument passed to it.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the returned `ShortUrl` object has the expected hash `"f684a3c4"`.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )
        val shortUrl = createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        println(shortUrl)

        assertEquals(shortUrl.hash, "f684a3c4")
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method throws an `InvalidUrlException`
     * if the provided URL is not valid.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `SafetyService`, 
     *    `HashService`, `ShortUrlProperties`, `BrandedService`, and `QrService`.
     * 2. Set up the mock to return `false` for `validatorService.isValid("ftp://example.com/")`.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"ftp://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the method throws an `InvalidUrlException`.
     */
    @Test
    fun `creates returns invalid URL exception if the URL is not valid`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val safetyService = mock<SafetyService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("ftp://example.com/")).thenReturn(false)

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )

        assertFailsWith<InvalidUrlException> {
            createShortUrlUseCase.create("ftp://example.com/", shortUrlProperties)
        }
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method throws an `InternalError`
     * if the URI cannot be validated due to an exception in the validator service.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `SafetyService`, 
     *    `HashService`, `ShortUrlProperties`, `BrandedService`, and `QrService`.
     * 2. Set up the mock to throw a `RuntimeException` for `validatorService.isValid("http://example.com/")`.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the method throws an `InternalError`.
     */
    @Test
    fun `creates returns invalid URL exception if the URI cannot be validated`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val safetyService = mock<SafetyService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenThrow(RuntimeException())

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method throws an `InternalError`
     * if the hash cannot be computed due to an exception in the hash service.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`, 
     *    `ShortUrlProperties`, `SafetyService`, `BrandedService`, and `QrService`.
     * 2. Set up the mocks to return expected values:
     *    - `validatorService.isValid("http://example.com/")` returns `true`.
     *    - `hashService.hasUrl("http://example.com/")` throws a `RuntimeException`.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the method throws an `InternalError`.
     */
    @Test
    fun `creates returns invalid URL exception if the hash cannot be computed`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val safetyService = mock<SafetyService>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenThrow(RuntimeException())

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method throws an `InternalError`
     * if the short URL cannot be saved due to an exception in the repository service.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`, 
     *    `ShortUrlProperties`, `SafetyService`, `BrandedService`, and `QrService`.
     * 2. Set up the mocks to return expected values:
     *    - `validatorService.isValid("http://example.com/")` returns `true`.
     *    - `hashService.hasUrl("http://example.com/")` returns `"f684a3c4"`.
     *    - `shortUrlRepository.save(any())` throws a `RuntimeException`.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the method throws an `InternalError`.
     */
    @Test
    fun `creates returns invalid URL exception if the short URL cannot be saved`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val safetyService = mock<SafetyService>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).thenThrow(RuntimeException())

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method returns a valid branded short URL
     * when the `isBranded` property is set to `true`.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`, 
     *    `SafetyService`, `ShortUrlProperties`, `BrandedService`, and `QrService`.
     * 2. Set up the mocks to return expected values:
     *    - `validatorService.isValid("http://example.com/")` returns `true`.
     *    - `shortUrlRepository.save(any())` returns the argument passed to it.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Create a `ShortUrlProperties` object with `isBranded` set to `true` and `name` set to `"branded"`.
     * 5. Call the `create` method with the URL `"http://example.com/"` and the `ShortUrlProperties` object.
     * 6. Verify that the returned `ShortUrl` object has the expected hash `"branded"`.
     */
    @Test 
    fun `creates returns a valid branded short URL`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )
        val properties = ShortUrlProperties(isBranded = true, name = "branded")
        val shortUrl = createShortUrlUseCase.create("http://example.com/", properties)

        assertEquals(shortUrl.hash, "branded")
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     *
     * This test verifies that the `create` method throws an `InvalidNameBrandedUrl`
     * exception if the `name` property is not provided for a branded URL.
     *
     * Steps:
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`, 
     *    `SafetyService`, `ShortUrlProperties`, `BrandedService`, and `QrService`.
     * 2. Set up the mocks to return expected values:
     *    - `validatorService.isValid("http://example.com/")` returns `true`.
     *    - `shortUrlRepository.save(any())` returns the argument passed to it.
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `ShortUrlProperties` object
     *    with `isBranded` set to `true` and `name` set to `null`.
     * 5. Verify that the method throws an `InvalidNameBrandedUrl` exception.
     */
    @Test
    fun `create a return invalid branded link exception if name is empty`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )
        assertFailsWith<EmptyNameBrandedUrl> {
            createShortUrlUseCase.create("http://example.com/", ShortUrlProperties(isBranded = true))
        }
    }

    /**
     * Test case for the `CreateShortUrlUseCaseImpl` class.
     * 
     * This test verifies that the `create` method throws a `LimitExceededException`
     * if the user has exceeded the limit of shortened URLs.
     * 1. Mock the dependencies: `ShortUrlRepositoryService`, `ValidatorService`, `HashService`,
     *   `ShortUrlProperties`, and `SafetyService`.
     * 2. Set up the mocks to return expected values:
     *  - `shortUrlRepository.countShortenedUrlsByUser("user123")
     * 3. Create an instance of `CreateShortUrlUseCaseImpl` with the mocked dependencies.
     * 4. Call the `create` method with the URL `"http://example.com/"` and `shortUrlProperties`.
     * 5. Verify that the method throws a `LimitExceededException`.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash and branded flag is desactivated`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val safetyService = mock<SafetyService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val brandedService = mock<BrandedService>()
        val qrService = mock<QrService>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hasUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase 
            = CreateShortUrlUseCaseImpl(
                shortUrlRepository, 
                validatorService, 
                hashService,
                safetyService,
                brandedService,
                qrService
            )
        val shortUrl = createShortUrlUseCase.create("http://example.com/", ShortUrlProperties(isBranded = false))

        assertEquals(shortUrl.hash, "f684a3c4")
    }

    /*
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
        println(shortUrlRepository.countShortenedUrlsByUser("user123"));

        // Expect a LimitExceededException to be thrown
        assertFailsWith<LimitExceededException> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }
    */
}
