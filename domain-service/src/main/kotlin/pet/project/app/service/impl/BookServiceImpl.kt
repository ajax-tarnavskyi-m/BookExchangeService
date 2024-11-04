package pet.project.app.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pet.project.app.annotation.Profiling
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.kafka.BookAmountIncreasedProducer
import pet.project.app.model.domain.DomainBook
import pet.project.app.repository.BookRepository
import pet.project.app.service.BookService
import pet.project.core.exception.BookNotFoundException
import reactor.core.publisher.Mono

@Profiling
@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val bookAmountIncreasedProducer: BookAmountIncreasedProducer,
) : BookService {

    override fun create(createBookRequest: CreateBookRequest): Mono<DomainBook> {
        return bookRepository.insert(createBookRequest)
    }

    override fun getById(bookId: String): Mono<DomainBook> {
        return bookRepository.findById(bookId)
            .switchIfEmpty(Mono.error { BookNotFoundException("Could not find book $bookId during GET request") })
    }

    override fun updateAmount(request: UpdateAmountRequest): Mono<Unit> {
        return bookRepository.updateAmount(request)
            .handle<Unit> { isUpdated, sink ->
                if (isUpdated) {
                    produceForNotificationIfDeltaPositive(listOf(request))
                } else {
                    sink.error(IllegalArgumentException("Book absent or no enough available: $request"))
                }
            }
            .thenReturn(Unit)
    }

    private fun produceForNotificationIfDeltaPositive(requestList: List<UpdateAmountRequest>) {
        requestList.filter { it.delta > 0 }
            .map { it.bookId }
            .let { if (it.isNotEmpty()) bookAmountIncreasedProducer.sendMessages(it) }
    }

    override fun exchangeBooks(requests: List<UpdateAmountRequest>): Mono<Unit> {
        return bookRepository.updateAmountMany(requests)
            .doOnSuccess { produceForNotificationIfDeltaPositive(requests) }
            .thenReturn(Unit)
    }

    override fun update(bookId: String, request: UpdateBookRequest): Mono<DomainBook> {
        return bookRepository.update(bookId, request)
            .switchIfEmpty(Mono.error { BookNotFoundException("Could not find book $bookId during Update request") })
    }

    override fun delete(bookId: String): Mono<Unit> {
        return bookRepository.delete(bookId)
            .doOnNext { deleteCount -> logIfBookNotFoundForDeletion(deleteCount, bookId) }
            .thenReturn(Unit)
    }

    private fun logIfBookNotFoundForDeletion(deleteCount: Long, bookId: String) {
        if (deleteCount != 1L) {
            log.warn("Affected {} documents while trying to delete book with id={}", deleteCount, bookId)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BookServiceImpl::class.java)
    }
}
