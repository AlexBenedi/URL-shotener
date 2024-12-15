package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ClickRepositoryService
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class LogClickUseCaseTest {

    /**
     * Tests that `logClick` fails silently when the repository throws an exception.
     *
     * Mocks the repository and click properties, and verifies that the method does not throw an exception
     * when the repository's `save` method throws a `RuntimeException`.
     */
    @Test
    fun `logClick fails silently`() {
        val repository = mock<ClickRepositoryService> ()
        val clickProperties = mock<ClickProperties>()
        whenever(repository.save(any())).thenThrow(RuntimeException())

        val useCase = LogClickUseCaseImpl(repository)

        useCase.logClick("key", clickProperties)
        verify(repository).save(any())
    }

    /**
     * Tests that `logClick` saves a new click when no previous click exists.
     *
     * Mocks the repository and click properties, and verifies that the method 
     * saves a new click when no previous click exists.
     */
    @Test
    fun `logClick saves a new click when no previous click exists`() {
        val repository = mock<ClickRepositoryService>()
        val clickProperties = ClickProperties(ip = "127.0.0.1")
        whenever(repository.findByHash("key")).thenReturn(null)

        val useCase = LogClickUseCaseImpl(repository)
        useCase.logClick("key", clickProperties)

        verify(repository).save(any())
    }

    /**
     * Tests that `logClick` updates the click count when a previous click exists.
     *
     * Mocks the repository and click properties, and verifies that the method updates 
     * the click count when a previous click exists.
     */
    @Test
    fun `logClick updates click count when previous click exists`() {
        val repository = mock<ClickRepositoryService>()
        val clickProperties = ClickProperties(ip = "127.0.0.1")
        val existingClick = Click(hash = "key", properties = clickProperties, clicks = 1)
        whenever(repository.findByHash("key")).thenReturn(existingClick)

        val useCase = LogClickUseCaseImpl(repository)
        useCase.logClick("key", clickProperties)

        verify(repository).updateClicksByHash("key", 2)
    }

    /**
     * Tests that `getTotalClicksByHash` returns the correct number of clicks.
     *
     * Mocks the repository and verifies that the method returns the correct number of clicks.
     */
    @Test
    fun `getTotalClicksByHash returns the correct number of clicks`() {
        val repository = mock<ClickRepositoryService>()
        whenever(repository.getTotalClicksByHash("key")).thenReturn(5)

        val useCase = LogClickUseCaseImpl(repository)
        val totalClicks = useCase.getTotalClicksByHash("key")

        assertEquals(5, totalClicks)
    }
}

