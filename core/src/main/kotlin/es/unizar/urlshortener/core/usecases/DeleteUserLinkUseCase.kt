@file:Suppress("WildcardImport")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.LinkRepositoryService

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface DeleteUserLinkUseCase {
    /**
     * Deletes a link from the database
     *
     * @param idLink The link id to be deleted
     */
    fun deleteById(idLink: Long)
}

/**
 * Implementation of [GetUserInformationUseCase].
 */
class DeleteUserLinkUseCaseImpl(
    private val linkRepository : LinkRepositoryService
) : DeleteUserLinkUseCase {

    /**
     * Deletes a link from the database
     *
     * @param idLink The link id to be deleted
     */
    override fun deleteById(idLink: Long) {
        linkRepository.deleteById(idLink)
    }
}
