package es.unizar.urlshortener.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


/**
 * Specification of the repository of [LinkEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface LinkEntityRepository : JpaRepository<LinkEntity, Long>{
    // Encuentra todas las entidades LinkEntity que coinciden con el userId
    @Query("SELECT l FROM LinkEntity l WHERE l.userId = :userId")
    fun findByUserId(@Param("userId") userId: UserEntity): List<LinkEntity>
}

/**
 * Specification of the repository of [UserEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface UserEntityRepository : JpaRepository<UserEntity, String>



/**
 * Specification of the repository of [ShortUrlEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ShortUrlEntityRepository : JpaRepository<ShortUrlEntity, String> {
    /**
     * Finds a [ShortUrlEntity] by its hash.
     *
     * @param hash The hash of the [ShortUrlEntity].
     * @return The found [ShortUrlEntity] or null if not found.
     */
    fun findByHash(hash: String): ShortUrlEntity?
}

/**
 * Specification of the repository of [ClickEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ClickEntityRepository : JpaRepository<ClickEntity, Long>
