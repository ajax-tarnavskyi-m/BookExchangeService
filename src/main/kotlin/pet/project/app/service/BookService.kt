package pet.project.app.service

import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.model.Book

interface BookService {

    fun create(book: Book): Book

    fun getById(bookId: String): Book

    fun update(book: Book): Book

    fun updateAmount(request: UpdateAmountRequest): Boolean

    fun delete(bookId: String)

    fun exchangeBooks(requests: List<UpdateAmountRequest>): Boolean
}
