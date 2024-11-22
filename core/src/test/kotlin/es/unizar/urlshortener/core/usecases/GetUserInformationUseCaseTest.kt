package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.UserRepositoryService
import es.unizar.urlshortener.core.LinkRepositoryService
import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.User
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetUserInformationUseCaseTest {

    private val userRepository = mock<UserRepositoryService>()
    private val linkRepository = mock<LinkRepositoryService>()
    private val getUserInformationUseCase = GetUserInformationUseCaseImpl(userRepository, linkRepository)

    @Test
    fun `should save user when it does not exist`() {
        val user = User("123456", 0, null)

        // Simular que el usuario no existe
        whenever(userRepository.findById(user.userId)).thenReturn(null)

        // Simular que el usuario es guardado
        whenever(userRepository.save(user)).thenReturn(user)

        // Llamar al método que deseas probar
        getUserInformationUseCase.processUser(user)

        // Verificar que se guardó el usuario
        verify(userRepository).save(user)
    }

    @Test
    fun `getLinks returns links for a user`() {
        val user = User("123456", 0, null)
        val link = Link(
            click = Click(
                hash = "kamalmola",
                properties = ClickProperties("192.168.1.1", "http://example.com", "Chrome", "Windows", "US"),
                created = OffsetDateTime.now(),
                clicks = 0
            ),
            shortUrl = ShortUrl(
                hash = "kamalmola",
                redirection = Redirection("http://example.com", 307),
                created = OffsetDateTime.now(),
                properties = ShortUrlProperties("192.168.1.1", "Sponsor", UrlSafetyResponse(true), "OwnerName", "US")
            ),
            user = user,
            id = 1
        )

        // Simular que existen enlaces para el usuario
        whenever(linkRepository.findByUserId(user)).thenReturn(listOf(link))

        // Ejecutar el caso de uso
        val links = getUserInformationUseCase.getLinks(user)

        // Verificar que los enlaces devueltos son correctos
        assertEquals(1, links.size)
        assertEquals("kamalmola", links[0].click.hash)
    }

    @Test
    fun `insertExampleLink saves link correctly`() {
        val user = User("123456", 0, null)
        val hashUrl = "examplehash"

        // Simular la llamada al método save del linkRepository
        whenever(linkRepository.save(any())).thenAnswer { it.arguments[0] as Link }

        // Ejecutar la inserción de un enlace de ejemplo
        getUserInformationUseCase.insertExampleLink(user, hashUrl)

        // Verificar que el enlace fue guardado correctamente
        // Aquí verificamos que el método save fue llamado en linkRepository
        verify(linkRepository).save(any())

        // También podemos comprobar que el hash del click guardado es el esperado
        // Aquí capturamos el argumento pasado a save
        val linkArgumentCaptor = argumentCaptor<Link>()
        verify(linkRepository).save(linkArgumentCaptor.capture())

        // Validar el valor del hash en el click
        assertEquals(linkArgumentCaptor.firstValue.click.hash, hashUrl)
    }

    @Test
    fun `processUser does not save user if it exists`() {
        val user = User("123456", 0, null)

        // Simular que el usuario ya existe en la base de datos
        whenever(userRepository.findById(user.userId)).thenReturn(user)

        // Ejecutar el caso de uso
        getUserInformationUseCase.processUser(user)

        // Verificar que el método save NO fue llamado
        verify(userRepository, never()).save(user)
    }

    @Test
    fun `getLinks returns empty list if no links exist for user`() {
        val user = User("123456", 0 , null)

        // Simular que no hay enlaces para el usuario
        whenever(linkRepository.findByUserId(user)).thenReturn(emptyList())

        // Ejecutar el caso de uso
        val links = getUserInformationUseCase.getLinks(user)

        // Verificar que no se devuelven enlaces
        assertEquals(0, links.size)
    }
}
