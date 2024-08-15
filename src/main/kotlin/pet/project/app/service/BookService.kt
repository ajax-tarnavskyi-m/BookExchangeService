package pet.project.app.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pet.project.app.repository.BookRepository
import pet.project.app.repository.model.Book

@Service
class BookService(val bookRepository: BookRepository) {

    fun create(book: Book) : Book {
        return bookRepository.save(book)
    }

    fun getById(id: Long): Book {
        return bookRepository.findByIdOrNull(id) ?: throw Exception("No book with id = $id")
    }

    fun update(id: Long, book: Book): Book {
        if (!bookRepository.existsById(id)) {
            throw Exception("No book with id = $id, to update")
        }
        return bookRepository.save(book)
    }

    fun increaseAmount(id: Long, addition: Int): Int {
        val book = getById(id)
        val updatedAmount : Int = (book.amountAvailable ?: 0) + addition
        val updatedBook = book.copy(amountAvailable = updatedAmount)
        bookRepository.save(updatedBook)
        if (updatedAmount == addition) {
            TODO("event for subscribers")
        }
        return updatedAmount
    }

    fun delete(id: Long) {
        if (!bookRepository.existsById(id)) {
            throw Exception("No book with id = $id to delete")
        }
        bookRepository.deleteById(id)
    }
}