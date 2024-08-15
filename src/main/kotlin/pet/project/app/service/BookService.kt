package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.repository.BookRepository
import pet.project.app.model.Book

@Service
class BookService(val bookRepository: BookRepository) {

    fun create(book: Book) : Book {
        return bookRepository.save(book)
    }

    fun getById(bookId: String): Book {
        return bookRepository.findByIdOrNull(bookId) ?: throw Exception("No book with id = $bookId")
    }

    fun update(bookId: String, book: Book): Book {
        if (!bookRepository.existsById(bookId)) {
            throw Exception("No book with id = $bookId, to update")
        }
        book.id = bookId
        return bookRepository.save(book)
    }

    fun increaseAmount(id: String, addition: Int): Int {
        val book = getById(id)
        val updatedAmount : Int = (book.amountAvailable ?: 0) + addition
        val updatedBook = book.copy(amountAvailable = updatedAmount)
        bookRepository.save(updatedBook)
        if (updatedAmount == addition) {
            TODO("event for subscribers")
        }
        return updatedAmount
    }

    fun delete(id: String) {
        if (!bookRepository.existsById(id)) {
            throw Exception("No book with id = $id to delete")
        }
        bookRepository.deleteById(id)
    }
}