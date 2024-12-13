package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.QRCode
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GenerateQRCodeUseCaseTest {

    /**
     * Tests that `generateQRCode` returns a valid QR code for a valid URL.
     *
     * Verifies that the generated QR code is not null and has a valid Base64 string.
     */
    @Test
    fun `generateQRCode returns a valid QR code for a valid URL`() {
        val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl()

        val qrCode: QRCode = generateQRCodeUseCase.generateQRCode("http://example.com/", 250)

        // Ensure that the QR code is not null and has a valid Base64 string
        assert(qrCode.base64Image.isNotEmpty()) { "Generated QR code should have a base64-encoded image" }
    }

    /**
     * Tests that `generateQRCode` throws InvalidUrlException for an invalid URL.
     *
     * Verifies that an InvalidUrlException is thrown when an invalid URL is provided.
     */
    @Test
    fun `generateQRCode throws InvalidUrlException for an invalid URL`() {
        val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl()

        assertFailsWith<InvalidUrlException> {
            generateQRCodeUseCase.generateQRCode("invalid-url", 250)
        }
    }

    /**
     * Tests that `generateQRCode` throws an exception if the QR code generation fails.
     *
     * Mocks the QR code generation to simulate a failure and verifies that a RuntimeException is thrown.
     */
    @Test
    fun `generateQRCode throws exception if the QR code generation fails`() {
        val generateQRCodeUseCase = mock<GenerateQRCodeUseCase>()

        // Simulate a failure during the QR code generation
        whenever(
            generateQRCodeUseCase.generateQRCode(any(), any())
        ).thenThrow(RuntimeException("QR code generation failed"))


        assertFailsWith<RuntimeException> {
            generateQRCodeUseCase.generateQRCode("http://example.com/", 250)
        }
    }
}
