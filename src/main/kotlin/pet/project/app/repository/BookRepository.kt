package pet.project.app.repository

import org.springframework.stereotype.Repository
import pet.project.app.model.Book

@Repository
class BookRepository {
    val bookMock = Book(1, "Title", "Desc", 2000, 9.99, 1)

    fun create(book: Book): Book {
        return bookMock
    }

    fun getById(id: Long): Book {
        return bookMock
    }

    fun update(id: Long, book: Book): Book {
        return bookMock
    }

    fun delete(id: Long): Book {
        return bookMock
    }

    fun increaseAmount(addition: Int): Book {
        return bookMock
    }
}