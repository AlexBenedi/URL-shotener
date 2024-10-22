@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.repositories

import jakarta.persistence.*
import java.time.OffsetDateTime

/**
 * The [ClickEntity] entity logs clicks.
 */
@Entity
@Table(name = "click")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class ClickEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long?,
    val hash: String,
    val created: OffsetDateTime,
    val ip: String?,
    val referrer: String?,
    val browser: String?,
    val platform: String?,
    val country: String?
)

/**
 * The [ShortUrlEntity] entity stores short urls.
 */
@Entity
@Table(name = "shorturl")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class ShortUrlEntity(
    @Id
    val hash: String,
    val target: String,
    val sponsor: String?,
    val created: OffsetDateTime,
    val owner: String?,
    val mode: Int,
    val safe: Boolean,
    val ip: String?,
    val country: String?
)

@Entity
@Table(name = "link")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class LinkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    val shortUrl: ShortUrlEntity, // Relación con ShortUrlEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "click_id", nullable = false)
    val click: ClickEntity, // Relación con ClickEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: Long? = null,

    //TODO: Add QR information
)

@Entity
@Table(name = "user")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(length = 50, nullable = false)
    val username: String,

    @Column(length = 255, nullable = false, unique = true)
    val email: String,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    val links: MutableList<LinkEntity> = mutableListOf()
)


