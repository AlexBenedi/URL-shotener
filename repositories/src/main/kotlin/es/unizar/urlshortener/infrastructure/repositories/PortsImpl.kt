package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.User
import es.unizar.urlshortener.core.UserRepositoryService
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.LinkRepositoryService


/**
 * Implementation of the port [LinkRepositoryService].
 */
class LinkRepositoryServiceImpl(
    private val linkEntityRepository: LinkEntityRepository
) : LinkRepositoryService {
    /**
     * Saves a [Link] entity to the repository.
     *
     * @param l The [Link] entity to be saved.
     * @return The saved [Link] entity.
     */
    override fun save(l: Link): Link = linkEntityRepository.save(l.toEntity()).toDomain()

    /**
     * Finds a [Link] entity by its user id.
     *
     * @param user The user id of the [Link] entity.
     * @return The found [Link] entity or null if not found.
     */
    override fun findByUserId(user: User): List<Link> {
        return linkEntityRepository.findByUserId(user.toEntity()).map { it.toDomain() }
    }

    /**
     * Deletes a [Link] entity from the repository.
     */
    override fun deleteById(idLink: Long) {
        linkEntityRepository.deleteById(idLink)
    }
}


/**
 * Implementation of the port [UserRepositoryService].
 *
 */
class UserRepositoryServiceImpl(
    private val userEntityRepository: UserEntityRepository
) : UserRepositoryService {
    /**
     * Finds a [User] entity by its id.
     *
     * @param id The id of the [User] entity.
     * @return The found [User] entity or null if not found.
     */
    override fun findById(id: String): User? {
        return userEntityRepository.findById(id).orElse(null)?.toDomain()
    }

    /**
     * Saves a [User] entity to the repository.
     *
     * @param u The [User] entity to be saved.
     * @return The saved [User] entity.
     */
    override fun save(u: User): User {
        return userEntityRepository.save(u.toEntity()).toDomain()
    }
}


/**
 * Implementation of the port [ClickRepositoryService].
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    /**
     * Saves a [Click] entity to the repository.
     *
     * @param cl The [Click] entity to be saved.
     * @return The saved [Click] entity.
     */
    override fun save(cl: Click): Click = clickEntityRepository.save(cl.toEntity()).toDomain()
}

/**
 * Implementation of the port [ShortUrlRepositoryService].
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    /**
     * Finds a [ShortUrl] entity by its key.
     *
     * @param id The key of the [ShortUrl] entity.
     * @return The found [ShortUrl] entity or null if not found.
     */
    override fun findByKey(id: String): ShortUrl? = shortUrlEntityRepository.findByHash(id)?.toDomain()

    /**
     * Saves a [ShortUrl] entity to the repository.
     *
     * @param su The [ShortUrl] entity to be saved.
     * @return The saved [ShortUrl] entity.
     */
    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()

    override fun countShortenedUrlsByUser(userId: String): Int {
        return shortUrlEntityRepository.countByOwner(userId)
    }
}
