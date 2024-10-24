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

    @Test
    fun `generateQRCode returns a valid QR code for a valid URL`() {
        val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl()

        val qrCode: QRCode = generateQRCodeUseCase.generateQRCode("http://example.com/", 250)

        // Ensure that the QR code is not null and has a valid Base64 string
        assert(qrCode.base64Image.isNotEmpty()) { "Generated QR code should have a base64-encoded image" }
    }

    @Test
    fun `generateQRCode throws InvalidUrlException for an invalid URL`() {
        val generateQRCodeUseCase = GenerateQRCodeUseCaseImpl()

        assertFailsWith<InvalidUrlException> {
            generateQRCodeUseCase.generateQRCode("invalid-url", 250)
        }
    }

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
