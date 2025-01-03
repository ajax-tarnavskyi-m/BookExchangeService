package pet.project.app.repository

import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import reactor.core.publisher.Mono

interface BookRepository {
    fun insert(createBookRequest: CreateBookRequest): Mono<DomainBook>
    fun findById(id: String): Mono<DomainBook>
    fun existsById(id: String): Mono<Boolean>
    fun updateAmount(request: UpdateAmountRequest): Mono<Boolean>
    fun updateAmountMany(requests: List<UpdateAmountRequest>): Mono<Int>
    fun updateShouldBeNotified(bookId: String, newValue: Boolean): Mono<Long>
    fun delete(id: String): Mono<Long>
    fun update(id: String, request: UpdateBookRequest): Mono<DomainBook>
}
