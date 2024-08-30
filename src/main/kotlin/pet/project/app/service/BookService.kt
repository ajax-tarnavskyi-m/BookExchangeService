package pet.project.app.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository
import kotlin.math.abs

@Service
class BookService(private val bookRepository: BookRepository) {

    fun create(book: Book): Book = bookRepository.save(book)

    fun getById(bookId: String): Book =
        bookRepository.findByIdOrNull(bookId) ?: throw BookNotFoundException(bookId, "GET request")

    fun update(book: Book): Book {
        if (bookRepository.existsById(book.id!!.toHexString())) {
            return bookRepository.save(book)
        } else {
            throw BookNotFoundException(book.id.toHexString(), "UPDATE request")
        }

    }

    @Transactional
    fun changeAmount(requests: List<UpdateAmountRequest>): Int {
        var booksAmountAffected = 0
        val requestedIds = requests.map { it.bookId }
        val booksByIds = bookRepository.findAllById(requestedIds)
        if (booksByIds.size != requests.size) {
            throw RuntimeException("Could not find all books. Found $booksByIds, while requested $requestedIds")
        }
        for (book in booksByIds) {
            val correspondingRequest = requests.find { it.bookId == book.id!!.toHexString() }!!
            val updatedAmount = (book.amountAvailable ?: 0) + correspondingRequest.delta
            check(updatedAmount >= 0) {
                "Can't withdraw ${abs(correspondingRequest.delta)} book(s), when amount is ${book.amountAvailable}"
            }
            val updatedBook = book.copy(amountAvailable = updatedAmount)
            bookRepository.save(updatedBook)
            booksAmountAffected += abs(correspondingRequest.delta)
        }
        return booksAmountAffected;
    }

    fun delete(bookId: String) {
        if (bookRepository.existsById(bookId)) {
            bookRepository.deleteById(bookId)
        } else {
            logger.warn { "Attempting to delete absent book with id=$bookId" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
