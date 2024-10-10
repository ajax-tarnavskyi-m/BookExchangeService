package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import pet.project.app.service.NotificationService
import java.util.concurrent.CompletableFuture

@Profiling
@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val notificationService: NotificationService,
) : BookService {

    override fun create(createBookRequest: CreateBookRequest): DomainBook {
        return bookRepository.insert(createBookRequest)
    }

    override fun getById(bookId: String): DomainBook =
        bookRepository.findById(bookId) ?: throw BookNotFoundException(bookId, "GET request")

    override fun updateAmount(request: UpdateAmountRequest): Boolean {
        val amountUpdated = bookRepository.updateAmount(request)
        require(amountUpdated) { "Requested book absent or have less amount available that needed: $request" }

        if (request.delta > 0) {
            CompletableFuture.runAsync { notificationService.notifySubscribedUsers(request.bookId) }
        }
        return true
    }

    override fun exchangeBooks(requests: List<UpdateAmountRequest>): Boolean {
        val matchedCount = bookRepository.updateAmountMany(requests)
        require(matchedCount == requests.size) { "Requested books absent or no enough available: $requests" }

        val booksWithIncreasedAmount = requests.filter { it.delta > 0 }.map { it.bookId }
        if (booksWithIncreasedAmount.isNotEmpty()) {
            CompletableFuture.runAsync { notificationService.notifySubscribedUsers(booksWithIncreasedAmount) }
        }
        return true
    }

    override fun update(bookId: String, request: UpdateBookRequest): DomainBook {
        return bookRepository.update(bookId, request) ?: throw BookNotFoundException(bookId, "Update request")
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
