package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import pet.project.app.service.NotificationService

@Profiling
@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val notificationService: NotificationService
) : BookService {

    override fun create(book: Book): Book = bookRepository.insert(book)

    override fun getById(bookId: String): Book =
        bookRepository.findByIdOrNull(bookId) ?: throw BookNotFoundException(bookId, "GET request")

    override fun update(book: Book): Book {
        val updatedDocumentsCount = bookRepository.update(book)
        if (updatedDocumentsCount != 1L) {
            throw BookNotFoundException(book.id!!.toHexString(), "UPDATE request")
        }
        return book
    }

    override fun updateAmount(request: UpdateAmountRequest): Boolean {
        val modifiedCount = bookRepository.updateAmount(request)
        if (modifiedCount != 1L) {
            throw IllegalArgumentException("Requested book absent or have less amount available that needed: $request")
        }
        if (request.delta > 0) {
            notificationService.notifySubscribedUsers(request.bookId)
        }
        return true
    }

    override fun exchangeBooks(requests: List<UpdateAmountRequest>): Boolean {
        val matchedCount = bookRepository.updateAmountMany(requests)
        if (matchedCount != requests.size) {
            throw IllegalArgumentException("Requested books absent or have less amount available that needed: $requests")
        }

        val booksWithIncreasedAmount = requests.filter { it.delta > 0 }
        if (booksWithIncreasedAmount.isNotEmpty()) {
            notificationService.notifySubscribedUsers(booksWithIncreasedAmount.map { it.bookId })
        }

        return true
    }

    override fun delete(bookId: String) {
        val deleteCount = bookRepository.delete(bookId)
        if (deleteCount != 1L) {
            log.warn("Affected {} documents while trying to delete book with id={}", deleteCount, bookId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookServiceImpl::class.java)
    }
}
