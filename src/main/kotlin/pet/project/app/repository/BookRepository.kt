package pet.project.app.repository

import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook

interface BookRepository {
    fun insert(createBookRequest: CreateBookRequest): DomainBook
    fun findById(id: String): DomainBook?
    fun existsById(id: String): Boolean
    fun updateAmount(request: UpdateAmountRequest): Boolean
    fun updateAmountMany(requests: List<UpdateAmountRequest>): Int
    fun updateShouldBeNotified(bookId: String, newValue: Boolean): Long
    fun delete(id: String): Long
    fun update(id: String, request: UpdateBookRequest): DomainBook?
}
