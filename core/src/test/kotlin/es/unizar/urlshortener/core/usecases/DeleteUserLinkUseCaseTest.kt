package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlSafetyResponse
import es.unizar.urlshortener.core.ShortUrlNotFoundException
import es.unizar.urlshortener.core.User
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import es.unizar.urlshortener.core.Link
import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.LinkRepositoryService

class DeleteUserLinkUseCaseTest {

    /**
     * Tests that `deleteById` delete the link with that Id.
     *
     * Mocks the linkRepository and verifies that deleteById is called with the given Id.
     */
    @Test
    fun `deleteById removes the link with the given Id`() {
        val linkRepository = mock<LinkRepositoryService>()
        val user = User(userId = "1", redirections = 5, lastRedirectionTimeStamp = null)

        whenever(linkRepository.deleteById(1L)).doAnswer { }

        val useCase = DeleteUserLinkUseCaseImpl(linkRepository)

        useCase.deleteById(1L)
        verify(linkRepository).deleteById(1L)

        whenever(linkRepository.findByUserId(user)).thenReturn(emptyList())
        val result = linkRepository.findByUserId(user)

        assertTrue(result.isEmpty())
    }
}

