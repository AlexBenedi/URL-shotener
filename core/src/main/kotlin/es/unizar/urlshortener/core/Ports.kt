package es.unizar.urlshortener.core

/**
 * [LinkRepositoryService] is the port to the repository that provides persistence to [Links][Link].
 */
interface LinkRepositoryService {
    /**
     * Saves a [Link] entity to the repository.
     *
     * @param l The [Link] entity to be saved.
     * @return The saved [Link] entity.
     */
    fun save(l: Link): Link


    /**
     * Finds a [Link] entity by its user id.
     *
     * @param userId The user id of the [Link] entity.
     * @return The found [Link] entity or null if not found.
     */
    fun findByUserId(user: User): List<Link>

    /**
        * Deletes a [Link] entity from the repository.
     */
    fun deleteById(idLink: Long)

}

/**
 * [UserRepositoryService] is the port to the repository that provides management to [Users][User].
 */
interface UserRepositoryService {
    /**
     * Finds a [User] entity by its id.
     *
     * @param id The id of the [User] entity.
     * @return The found [User] entity or null if not found.
     */
    fun findById(id: String): User?


    /**
     * Saves a [User] entity to the repository.
     *
     * @param u The [User] entity to be saved.
     * @return The saved [User] entity.
     */
    fun save(u: User): User
}


/**
 * [ClickRepositoryService] is the port to the repository that provides persistence to [Clicks][Click].
 */
interface ClickRepositoryService {
    /**
     * Saves a [Click] entity to the repository.
     *
     * @param cl The [Click] entity to be saved.
     * @return The saved [Click] entity.
     */
    fun save(cl: Click): Click

    fun findByHash(hash: String): Click?

    fun updateClicksByHash(hash: String, clicks: Int): Int

    fun getTotalClicksByHash(hash: String): Int

}

/**
 * [ShortUrlRepositoryService] is the port to the repository that provides management to [ShortUrl][ShortUrl].
 */
interface ShortUrlRepositoryService {
    /**
     * Finds a [ShortUrl] entity by its key.
     *
     * @param id The key of the [ShortUrl] entity.
     * @return The found [ShortUrl] entity or null if not found.
     */
    fun findByKey(id: String): ShortUrl?

    /**
     * Saves a [ShortUrl] entity to the repository.
     *
     * @param su The [ShortUrl] entity to be saved.
     * @return The saved [ShortUrl] entity.
     */
    fun save(su: ShortUrl): ShortUrl

    /**
     * Counts the number of shortened URLs for a specific user.
     *
     * @param userId The ID of the user.
     * @return The count of shortened URLs.
     */
    fun countShortenedUrlsByUser(userId: String): Int
}

/**
 * [ValidatorService] is the port to the service that validates if an url can be shortened.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface ValidatorService {
    /**
     * Validates if the given URL can be shortened.
     *
     * @param url The URL to be validated.
     * @return True if the URL is valid, false otherwise.
     */
    fun isValid(url: String): Boolean

    /**
     * Validates if the given id can be used.
     *
     * @param id The id to be validated.
     * @return True if the id is valid, false otherwise.
     */
    fun isValidBrandedUrl(id: String?): Boolean
}

/**
 * [HashService] is the port to the service that creates a hash from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface HashService {
    /**
     * Creates a hash from the given URL.
     *
     * @param url The URL to be hashed.
     * @return The hash of the URL.
     */
    fun hasUrl(url: String): String
}

/**
 * [SafetyService] is the port to the service that checks if an URL is safe.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface SafetyService {
    /**
     * Checks if the given URL is safe.
     * 
     * @param petition The petition to check the safety of a URL. 
     *        Contains the URL and an its ID.
     * @return True if the URL is safe, false otherwise.
     */
    fun isUrlSafe(petition: UrlSafetyPetition)
}


/**
 * [IntegrationService] is the port to the service that sends messages to spring integration channels.
 * 
 * **Note**: It is a design decision to create this port. It could be part of the core.
 */
interface IntegrationService {
    /**
     * Sends a message to the QR channel.
     * 
     * @param data The data to be sent.
     */
    fun sendQrMessage(data: Any)

    /**
     * Sends a message to the Branded channel.
     * 
     * @param data The data to be sent.
     */
    fun sendBrandedMessage(data: Any)
}
