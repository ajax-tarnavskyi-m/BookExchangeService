package pet.project.app.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

@Service
class BookService(private val bookRepository: BookRepository) {

    fun create(book: Book): Book = bookRepository.save(book)

    fun getById(bookId: String): Book =
        bookRepository.findByIdOrNull(bookId) ?: throw BookNotFoundException(bookId, "GET request")

    fun update(book: Book): Book {
        if (bookRepository.existsById(book.id!!)) {
            return bookRepository.save(book)
        } else {
            throw BookNotFoundException(book.id, "UPDATE request")
        }

    }

    fun increaseAmount(bookId: String, addition: Int): Int {
        val book = getById(bookId)
        val updatedAmount = (book.amountAvailable ?: 0) + addition
        val updatedBook = book.copy(amountAvailable = updatedAmount)
        bookRepository.save(updatedBook)
        if (updatedAmount == addition) {
            logger.info { "Message for subscribers of book (id=$bookId) was sent" }
        }
        return updatedAmount
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
