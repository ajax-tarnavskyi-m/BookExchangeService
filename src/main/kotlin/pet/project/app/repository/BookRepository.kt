package pet.project.app.repository

import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.model.Book

interface BookRepository {
    fun insert(book: Book): Book
    fun findByIdOrNull(id: String): Book?
    fun update(book: Book): Long
    fun delete(id: String): Long
    fun existsById(id: String): Boolean
    fun updateAmount(request: UpdateAmountRequest): Long
    fun updateAmountMany(requests: List<UpdateAmountRequest>): Int
    fun setShouldBeNotified(bookId: String, boolValue: Boolean): Long
}
