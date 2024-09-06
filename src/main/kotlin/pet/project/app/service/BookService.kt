package pet.project.app.service

import pet.project.app.model.Book

interface BookService {

    fun create(book: Book): Book

    fun getById(bookId: String): Book

    fun update(book: Book): Book

    fun changeAmount(bookId: String, delta: Int): Int

    fun delete(bookId: String)
}
