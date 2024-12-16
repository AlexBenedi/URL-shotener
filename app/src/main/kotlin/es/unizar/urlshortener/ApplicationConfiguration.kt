@file:Suppress("WildcardImport")

package es.unizar.urlshortener

import es.unizar.urlshortener.infrastructure.delivery.*
import es.unizar.urlshortener.infrastructure.repositories.ClickEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ClickRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.UserEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.UserRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.LinkEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.LinkRepositoryServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import es.unizar.urlshortener.websockets.WebSocketsServer

/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Configuration
class ApplicationConfiguration(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
    @Autowired val userEntityRepository: UserEntityRepository,
    @Autowired val linkEntityRepository: LinkEntityRepository,
    @Autowired val webSocketsServer: WebSocketsServer
    
) {

    /**
     * Configures the security filter chain.
     * @param http the [HttpSecurity] object.
     * @return the [SecurityFilterChain] object.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // Desactiva CSRF para simplificar el acceso
            .authorizeHttpRequests { registry ->
                registry.requestMatchers("/ws-endpoint").permitAll()  // Permitir acceso al WebSocket
                registry.requestMatchers("/user").authenticated()  // Rutas autenticadas
                registry.requestMatchers("/api/users/{userId}/links").authenticated()
                registry.requestMatchers("/clicks/{hash}").authenticated()
                registry.requestMatchers("/delete/{idLink}").authenticated()
                registry.requestMatchers("/api/link/user/{userId}").authenticated()
                registry.anyRequest().permitAll()  // Rutas públicas
            }
            .oauth2Login(Customizer.withDefaults())  // Configura OAuth2 Login
            .logout { logout ->
                logout
                    .logoutUrl("/logout")  // Define la URL para hacer logout
                    .logoutSuccessUrl("/")  // Redirige al inicio después de hacer logout
                    .invalidateHttpSession(true)  // Invalida la sesión HTTP
                    .clearAuthentication(true)  // Limpia el contexto de seguridad
                    .deleteCookies("JSESSIONID")  // Elimina las cookies asociadas a la sesión
            }
            .build()
    }

    /**
     * Provides an implementation of the ClickRepositoryService.
     * @return an instance of ClickRepositoryServiceImpl.
     */
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    /**
     * Provides an implementation of the ShortUrlRepositoryService.
     * @return an instance of ShortUrlRepositoryServiceImpl.
     */
    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)
    
    /**
     * Provides an implementation of the UserRepositoryService.
     * @return an instance of UserRepositoryServiceImpl.
     */
    @Bean
    fun userRepositoryService() = UserRepositoryServiceImpl(userEntityRepository)
    
    /**
     * Provides an implementation of the LinkRepositoryService.
     * @return an instance of LinkRepositoryServiceImpl.
     */
    @Bean
    fun linkRepositoryService() = LinkRepositoryServiceImpl(linkEntityRepository)

    /**
     * Provides an implementation of the ValidatorService.
     * @return an instance of ValidatorServiceImpl.
     */
    @Bean
    fun validatorService() = ValidatorServiceImpl()

    /**
     * Provides an implementation of the HashService.
     * @return an instance of HashServiceImpl.
     */
    @Bean
    fun hashService() = HashServiceImpl()

    /**
     * Provides an implementation of the sockets service
     */
    @Bean
    fun webSocketsService() = WebSocketsServiceImpl( webSocketsServer )



}
