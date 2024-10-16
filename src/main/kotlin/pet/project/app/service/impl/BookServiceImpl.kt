package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.exception.BookNotFoundException
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@Profiling
@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val availableBooksSink: Sinks.Many<String>,
) : BookService {

    override fun create(createBookRequest: CreateBookRequest): Mono<DomainBook> {
        return bookRepository.insert(createBookRequest)
    }

    override fun getById(bookId: String): Mono<DomainBook> {
        return bookRepository.findById(bookId)
            .switchIfEmpty(Mono.error { BookNotFoundException(bookId, "GET request") })
    }

    override fun updateAmount(request: UpdateAmountRequest): Mono<Unit> {
        return bookRepository.updateAmount(request)
            .handle { isUpdated, sink ->
                if (isUpdated) {
                    sendForNotificationIfDeltaPositive(request)
                    sink.next(Unit)
                } else {
                    sink.error(IllegalArgumentException("Book absent or no enough available: $request"))
                }
            }
    }

    private fun sendForNotificationIfDeltaPositive(request: UpdateAmountRequest) {
        if (request.delta > 0) availableBooksSink.tryEmitNext(request.bookId)
    }

    override fun exchangeBooks(requests: List<UpdateAmountRequest>): Mono<Unit> {
        return bookRepository.updateAmountMany(requests)
            .doOnSuccess { notifyAboutNewBooks(requests) }
            .thenReturn(Unit)
    }

    private fun notifyAboutNewBooks(requests: List<UpdateAmountRequest>) {
        requests.filter { request -> request.delta > 0 }
            .forEach { request -> availableBooksSink.tryEmitNext(request.bookId) }
    }

    override fun update(bookId: String, request: UpdateBookRequest): Mono<DomainBook> {
        return bookRepository.update(bookId, request)
            .switchIfEmpty(Mono.error { BookNotFoundException(bookId, "Update request") })
    }

    override fun delete(bookId: String): Mono<Unit> {
        return bookRepository.delete(bookId)
            .doOnNext { deleteCount -> if (deleteCount != 1L) log.warn(DELETE_WARN_MESSAGE, deleteCount, bookId) }
            .thenReturn(Unit)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookServiceImpl::class.java)
        private const val DELETE_WARN_MESSAGE = "Affected {} documents while trying to delete book with id={}"
    }
}

