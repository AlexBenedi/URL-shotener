@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.repositories

import jakarta.persistence.*
import java.time.LocalDateTime
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
    val country: String?,
    val clicks : Int
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
    val isSafe: Boolean?,
    val threatType: String? = null, 
    val platformType: String? = null, 
    val threatEntryType: String? = null, 
    val threatInfo: String? = null,
    val ip: String?,
    val country: String?,
    val isBranded: Boolean?,
    @Column(length = 65535)
    val qrCode: String?
)

@Entity
@Table(name = "link")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class LinkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long?,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "short_url_id", nullable = false)
    val shortUrl: ShortUrlEntity, // Relación con ShortUrlEntity

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "click_id", nullable = false)
    val click: ClickEntity, // Relación con ClickEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity
)

@Entity
@Table(name = "user")
@Suppress("LongParameterList", "JpaObjectClassSignatureInspection")
class UserEntity(
    @Id
    val id: String,
    val redirections : Int,
    val lastRedirectionTimeStamp : OffsetDateTime? = null

)


