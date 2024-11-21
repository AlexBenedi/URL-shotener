@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.*

/**
 * Extension method to convert a [ClickEntity] into a domain [Click].
 */
fun ClickEntity.toDomain() = Click(
    hash = hash,
    created = created,
    properties = ClickProperties(
        ip = ip,
        referrer = referrer,
        browser = browser,
        platform = platform,
        country = country
    ),
    clicks = clicks
)

/**
 * Extension method to convert a domain [Click] into a [ClickEntity].
 */
fun Click.toEntity() = ClickEntity(
    id = null,
    hash = hash,
    created = created,
    ip = properties.ip,
    referrer = properties.referrer,
    browser = properties.browser,
    platform = properties.platform,
    country = properties.country,
    clicks = 0
)

/**
 * Extension method to convert a [ShortUrlEntity] into a domain [ShortUrl].
 */
fun ShortUrlEntity.toDomain() = ShortUrl(
    hash = hash,
    redirection = Redirection(
        target = target,
        mode = mode
    ),
    created = created,
    properties = ShortUrlProperties(
        sponsor = sponsor,
        owner = owner,
        safe = UrlSafetyResponse(
            isSafe = isSafe,
            threatType = threatType,
            platformType = platformType,
            threatEntryType = threatEntryType,
            threatInfo = threatInfo
        ),
        ip = ip,
        country = country, 
        isBranded = isBranded,
        qrCode = qrCode
    )
)

/**
 * Extension method to convert a domain [ShortUrl] into a [ShortUrlEntity].
 */
fun ShortUrl.toEntity() = ShortUrlEntity(
    hash = hash,
    target = redirection.target,
    mode = redirection.mode,
    created = created,
    owner = properties.owner,
    sponsor = properties.sponsor,
    isSafe = properties.safe?.isSafe,
    threatType = properties.safe?.threatType,
    platformType = properties.safe?.platformType,
    threatEntryType = properties.safe?.threatEntryType,
    threatInfo = properties.safe?.threatInfo,
    ip = properties.ip,
    country = properties.country,
    isBranded = properties.isBranded,
    qrCode = properties.qrCode
)

/**
 * Extension method to convert a [LinkEntity] into a domain [Link].
 */
fun LinkEntity.toDomain() = Link(
    click = click.toDomain(),
    shortUrl = shortUrl.toDomain(),
    user = user.toDomain()
)

/**
 * Extension method to convert a domain [Link] into a [LinkEntity].
 */
fun Link.toEntity() = LinkEntity(
    id = null,
    shortUrl = shortUrl.toEntity(),
    click = click.toEntity(),
    user = user.toEntity()
)

/**
 * Extension method to convert a [UserEntity] into a domain [User].
 */
fun UserEntity.toDomain() = User(
    userId = id,
    redirections = redirections,
    lastRedirectionTimeStamp = lastRedirectionTimeStamp
)

/**
 * Extension method to convert a domain [User] into a [UserEntity].
 */
fun User.toEntity() = UserEntity(
    id = userId,
    redirections = redirections,
    lastRedirectionTimeStamp = lastRedirectionTimeStamp
)




