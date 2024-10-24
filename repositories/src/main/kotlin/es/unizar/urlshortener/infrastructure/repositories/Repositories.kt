package es.unizar.urlshortener.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


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

    @Query("SELECT COUNT(s) FROM ShortUrlEntity s WHERE s.owner = :userId")
    fun countByOwner(@Param("userId") userId: String): Int
}

/**
 * Specification of the repository of [ClickEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ClickEntityRepository : JpaRepository<ClickEntity, Long>
