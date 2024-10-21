package pet.project.app.service

import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import reactor.core.publisher.Mono

interface BookService {

    fun create(createBookRequest: CreateBookRequest): Mono<DomainBook>

    fun getById(bookId: String): Mono<DomainBook>

    fun updateAmount(request: UpdateAmountRequest): Mono<Unit>

    fun exchangeBooks(requests: List<UpdateAmountRequest>): Mono<Unit>

    fun update(bookId: String, request: UpdateBookRequest): Mono<DomainBook>

    fun delete(bookId: String): Mono<Unit>
}
