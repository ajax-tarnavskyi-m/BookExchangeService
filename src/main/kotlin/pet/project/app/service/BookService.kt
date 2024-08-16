package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

@Service
class BookService(private val bookRepository: BookRepository) {

    fun create(book: Book): Book {
        return bookRepository.save(book)
    }

    fun getById(bookId: String): Book {
        return bookRepository.findByIdOrNull(bookId) ?: throw Exception("No book with id = $bookId")
    }

    fun update(bookId: String, book: Book): Book {
        if (bookRepository.existsById(bookId)) {
            book.id = bookId
            return bookRepository.save(book)
        } else {
            throw Exception("No book with id = $bookId, to update")
        }

    }
    fun increaseAmount(id: String, addition: Int): Int {
        val book = getById(id)
        val updatedAmount: Int = (book.amountAvailable ?: 0) + addition
        val updatedBook = book.copy(amountAvailable = updatedAmount)
            .apply { this.id = id }
        bookRepository.save(updatedBook)
        if (updatedAmount == addition) {
            println("Message for subscribers of book (id=$id) was sent")
        }
        return updatedAmount
    }

    fun delete(id: String) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id)
        }
        throw Exception("No book with id = $id to delete")
    }
}
