package pet.project.app.service

import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook

interface BookService {

    fun create(createBookRequest: CreateBookRequest): DomainBook

    fun getById(bookId: String): DomainBook

    fun updateAmount(request: UpdateAmountRequest): Boolean

    fun exchangeBooks(requests: List<UpdateAmountRequest>): Boolean

    fun update(bookId: String, request: UpdateBookRequest): DomainBook

    fun delete(bookId: String)
}
