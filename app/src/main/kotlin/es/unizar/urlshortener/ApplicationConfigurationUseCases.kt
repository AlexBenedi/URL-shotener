package es.unizar.urlshortener

import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCaseImpl
import es.unizar.urlshortener.core.usecases.GetUserInformationUseCaseImpl
import es.unizar.urlshortener.core.usecases.LogClickUseCaseImpl
import es.unizar.urlshortener.core.usecases.RedirectUseCaseImpl
import es.unizar.urlshortener.core.usecases.UpdateUrlSafetyUseCaseImpl
import es.unizar.urlshortener.core.usecases.UpdateUrlBrandedUseCaseImpl
import es.unizar.urlshortener.ApplicationConfiguration
import es.unizar.urlshortener.infrastructure.delivery.HashServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.SafetyServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.BrandedServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ClickEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ClickRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.UserEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.UserRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.LinkEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.LinkRepositoryServiceImpl
import es.unizar.urlshortener.core.usecases.GenerateQRCodeUseCaseImpl
import es.unizar.urlshortener.core.usecases.DeleteUserLinkUseCaseImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfigurationUseCases(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
    @Autowired val userEntityRepository: UserEntityRepository,
    @Autowired val linkEntityRepository: LinkEntityRepository,
    @Autowired val safetyServiceImpl: SafetyServiceImpl,
    @Autowired val brandedServiceImpl: BrandedServiceImpl
){

    /**
     * Provides an implementation of the RedirectUseCase.
     * @return an instance of RedirectUseCaseImpl.
     */
    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(ShortUrlRepositoryServiceImpl(shortUrlEntityRepository))

    /**
     * Provides an implementation of the LogClickUseCase.
     * @return an instance of LogClickUseCaseImpl.
     */
    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(ClickRepositoryServiceImpl(clickEntityRepository))


    /**
     * Provides an implementation of the CreateShortUrlUseCase.
     * @return an instance of CreateShortUrlUseCaseImpl.
     */
    @Bean
    fun createShortUrlUseCase() =
        CreateShortUrlUseCaseImpl(ShortUrlRepositoryServiceImpl(shortUrlEntityRepository),
            ValidatorServiceImpl(),
            HashServiceImpl(),
            safetyServiceImpl,
            brandedServiceImpl
        )

    /**
     * Provides an implementation of the UpdateUrlSafetyUseCase.
     * @return an instance of UpdateUrlSafetyUseCaseImpl.
     */
    @Bean
    fun updateUrlSafetyUseCase() = 
        UpdateUrlSafetyUseCaseImpl(
            ShortUrlRepositoryServiceImpl(shortUrlEntityRepository),
    )

    /**
     * Provides an implementation of the GetUserInformationUseCase.
     * @return an instance of GetUserInformationUseCaseImpl.
     */
    @Bean
    fun getUserInformationUseCase() = GetUserInformationUseCaseImpl(
        UserRepositoryServiceImpl(userEntityRepository),
        LinkRepositoryServiceImpl(linkEntityRepository)
    )

    /**
     * Provides an implementation of the DeleteUserLinkUseCase.
     * @return an instance of DeleteUserLinkUseCaseImpl.
     */
    @Bean
    fun deleteUserLinkUseCase() = DeleteUserLinkUseCaseImpl(
        LinkRepositoryServiceImpl(linkEntityRepository)
    )

    /**
     * Provude an implementation of GenerateQRCodeUseCase.
     * @return an instance of GenerateQRCodeUseCaseImpl.
     */
    @Bean
    fun generateQRCodeUseCase() = GenerateQRCodeUseCaseImpl()

    /**
     * Provides an implementation of the UpdateUrlBrandedUseCase.
     * @return an instance of UpdateUrlBrandedUseCaseImpl.
     */
    @Bean
    fun updateUrlBrandedUseCase() = UpdateUrlBrandedUseCaseImpl(
        ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)
    )

}
