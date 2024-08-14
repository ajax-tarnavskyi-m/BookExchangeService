package pet.project.app.service

import org.springframework.stereotype.Service
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

@Service
class BookService(val bookRepository: BookRepository) {

    fun create(book: Book) : Book {
        return bookRepository.create(book)
    }

    fun getById(id: Long): Book {
        return bookRepository.getById(id)
    }

    fun update(id: Long, book: Book): Book {
        return bookRepository.update(id, book)
    }

    fun delete(id: Long): Book {
        return bookRepository.delete(id)
    }

    fun increaseAmount(id: Long, addition: Int): Int {
        val newAmount = bookRepository.increaseAmount(addition).amountAvailable
        if (newAmount == 1) {
            //make ivent for subscribers
        }
        return newAmount

    }
}