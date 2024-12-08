package es.unizar.urlshortener.core

import java.lang.RuntimeException

/**
 * A base class for domain-specific exceptions in the application.
 * This sealed class serves as a root for all exceptions related to the domain logic.
 * It extends [RuntimeException], allowing for an optional [cause].
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception, which can be null.
 */
sealed class DomainException(message: String, cause: Throwable? = null):
    RuntimeException(message, cause)

/**
 * An exception indicating that a provided URL does not follow a supported schema.
 * This exception is thrown when the format or schema of a URL does not match the expected pattern.
 *
 * @param url The URL that caused the exception.
 */
class InvalidUrlException(url: String) : DomainException("[$url] does not follow a supported schema")

class LimitExceededException(message: String) : RuntimeException(message)


/**
 * An exception indicating that a provided URL is considered unsafe.
 * This exception is thrown when a URL is deemed unsafe by a safety service.
 *
 * @param url The URL that is considered unsafe.
 */
class UnsafeUrlException(
    threatType: String,
    platformType: String, 
    threatEntryType: String, 
    threatInfo: String
) : DomainException(
    """
    This URL is considered unsafe!.
    Threat Type: $threatType
    Platform Type: $platformType
    Threat Entry Type: $threatEntryType
    Threat Info: $threatInfo
    """.trimIndent()
)

/**
 * An exception indicating that the safety of a URL has not been checked.
 * This exception is thrown when the safety of a URL has not been verified before use.
 */
class UrlSafetyNotCheckedException : DomainException("The safety of the URL has not been checked")

/**
 * An exception indicating that a short URL could not be found.
 * This exception is thrown when a specified short URL does not exist in the system.
 *
 * @param url The short URL that was not found.
 */
class ShortUrlNotFoundException(url: String) : DomainException("Short URL not found for [$url]")

/**
 * An exception indicating that a redirection key could not be found.
 * This exception is thrown when a specified redirection key does not exist in the system.
 *
 * @param key The redirection key that was not found.
 */
class RedirectionNotFound(key: String) : DomainException("[$key] is not known")

/**
 * An exception indicating that the id of the branded link must not be empty.
 * This exception is thrown when the flag isBranded is activate but the id is empty.
 */
class EmptyNameBrandedUrl : DomainException("Name must not be empty")

/**
 * An exception indicating that the name of a Branded link has not been checked.
 * This exception is thrown when the name of a branded link has not been verified before use.
 */
class BrandedNotCheckedException : DomainException("The name of the branded link has not been checked yet")

/**
 * An exception indicating that the name of a Branded link is invalid.
 * This exception is thrown when the name of a branded link is not valid.
 */
class InvalidNameBrandedUrl(key: String) : DomainException("[$key] is not a valid branded link")

/**
 * An exception indicating an internal error within the application.
 * This exception can be used to represent unexpected issues that occur within the application,
 * providing both a message and a cause for the error.
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception.
 */
class InternalError(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

inline fun <T> safeCall(
    onFailure: (Throwable) -> Throwable = { e -> InternalError("Unexpected error", e) },
    block: () -> T
): T = runCatching {
    block()
}.fold(
    onSuccess = { it },
    onFailure = { throw onFailure(it) }
)

/**
 * An exception indicating that a Short URL for a given URL could not be found.
 * This exception is thrown when attempting to retrieve a non-existent Short URL.
 *
 * @param url The URL that was not found.
 */
class UrlNotFoundException(url: String) : DomainException("Short URL not found for [$url]")
