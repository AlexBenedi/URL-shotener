package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.UnsafeUrlException
import es.unizar.urlshortener.core.UrlSafetyNotCheckedException
import es.unizar.urlshortener.core.ShortUrlNotFoundException
import es.unizar.urlshortener.core.BrandedNotCheckedException
import es.unizar.urlshortener.core.EmptyNameBrandedUrl
import es.unizar.urlshortener.core.InvalidNameBrandedUrl
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * A controller advice to handle exceptions globally and return appropriate HTTP responses.
 */
@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    /**
     * Handles InvalidUrlException and returns a BAD_REQUEST response.
     *
     * @param ex the InvalidUrlException thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [InvalidUrlException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidUrls(ex: InvalidUrlException) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

    /**
     * Handles UnsafeUrlException and returns a FORBIDDEN response.
     * 
     * @param ex the UnsafeUrlException thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [UnsafeUrlException::class])
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun unsafeUrl(ex: UnsafeUrlException) = ErrorMessage(HttpStatus.FORBIDDEN.value(), ex.message)

    /**
     * Handles UrlSafetyNotCheckedException and returns a BAD_REQUEST response.
     * 
     * @param ex the UrlSafetyNotCheckedException thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [UrlSafetyNotCheckedException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun urlSafetyNotcheked(ex: UrlSafetyNotCheckedException) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

    /**
     * Handles RedirectionNotFound exception and returns a NOT_FOUND response.
     *
     * @param ex the RedirectionNotFound exception thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [RedirectionNotFound::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun redirectionNotFound(ex: RedirectionNotFound) = ErrorMessage(HttpStatus.NOT_FOUND.value(), ex.message)

    /**
     * Handles emptyNameBrandedUrl exception and returns a BAD_REQUEST response.
     *
     * @param ex the EmptyNameBrandedUrl exception thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [EmptyNameBrandedUrl::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun emptyNameBrandedUrl(ex: EmptyNameBrandedUrl) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

        /**
     * Handles InvalidNameBrandedUrl exception and returns a NOT_FOUND response.
     *
     * @param ex the InvalidNameBrandedUrl exception thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [InvalidNameBrandedUrl::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun invalidNameBrandedUrl(ex: InvalidNameBrandedUrl) = ErrorMessage(HttpStatus.NOT_FOUND.value(), ex.message)

    /**
     * Handles BrandedNotChecked exception and returns a BAD_REQUEST response.
     *
     * @param ex the BrandedNotCheckedException exception thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [BrandedNotCheckedException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun brandedNotChecked(ex: BrandedNotCheckedException) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

    /**
     * Handles ShortUrlNotFoundException and returns a INTERNAL_SERVER response.
     * 
     * @param ex the ShortUrlNotFoundException thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [ShortUrlNotFoundException::class])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun shortUrlNotFound(ex: ShortUrlNotFoundException) = 
        ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message)
    /**
     * Handles InternalError and returns an INTERNAL_SERVER_ERROR response.
     *
     * @param ex the InternalError thrown
     * @param request the WebRequest during which the exception was thrown
     * @return an ErrorMessage containing the status code and exception message
     */
    @ResponseBody
    @ExceptionHandler(value = [InternalError::class])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalError(ex: InternalError, request: WebRequest): ErrorMessage {
        log.error("Internal error: ${ex.message}, Request Details: ${request.getDescription(false)}", ex)
        return ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler::class.java)
    }
}

/**
 * Data class representing an error message to be returned in the response body.
 *
 * @property statusCode the HTTP status code
 * @property message the error message
 * @property timestamp the timestamp when the error occurred
 */
data class ErrorMessage(
    val statusCode: Int,
    val message: String?,
    val timestamp: String = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())
)
