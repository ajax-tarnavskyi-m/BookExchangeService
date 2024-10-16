package pet.project.app.repository.impl

import org.springframework.data.mongodb.core.BulkOperations.BulkMode
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.ReactiveBulkOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate.update
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Add
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.valueOf
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.updateFirst
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import pet.project.app.annotation.Profiling
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.mapper.BookMapper.toDomain
import pet.project.app.mapper.BookMapper.toMongo
import pet.project.app.mapper.BookMapper.toUpdate
import pet.project.app.model.domain.DomainBook
import pet.project.app.model.mongo.MongoBook
import pet.project.app.repository.BookRepository
import reactor.core.publisher.Mono

@Repository
@Profiling
@Suppress("TooManyFunctions")
internal class BookMongoRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val transactionalOperator: TransactionalOperator,
) : BookRepository {

    override fun insert(createBookRequest: CreateBookRequest): Mono<DomainBook> {
        return mongoTemplate.insert(createBookRequest.toMongo())
            .map { mongoBook -> mongoBook.toDomain() }
    }

    override fun findById(id: String): Mono<DomainBook> {
        return mongoTemplate.findById(id, MongoBook::class.java)
            .map { mongoBook -> mongoBook.toDomain() }
    }

    override fun existsById(id: String): Mono<Boolean> {
        return mongoTemplate.exists<MongoBook>(whereId(id))
    }

    override fun updateAmount(request: UpdateAmountRequest): Mono<Boolean> {
        val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
        val result = mongoTemplate.updateFirst<MongoBook>(query, withAmountUpdatePipeline(request.delta))
        return result.map { it.modifiedCount == 1L }
    }

    override fun updateAmountMany(requests: List<UpdateAmountRequest>): Mono<Int> {
        val bulkOps: ReactiveBulkOperations = mongoTemplate.bulkOps(BulkMode.ORDERED, MongoBook::class.java)
        for (request in requests) {
            val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
            bulkOps.updateOne(query, withAmountUpdatePipeline(request.delta))
        }
        return bulkOps.execute()
            .handle { result, sink ->
                when (result.matchedCount != requests.size) {
                    true -> sink.error(IllegalArgumentException("Not existing ids or not enough books: $requests"))
                    false -> sink.next(result.matchedCount)
                }
            }
            .`as` { transactionalOperator.transactional(it) }
    }

    private fun withAmountUpdatePipeline(delta: Int): AggregationUpdate {
        val ifAmountWasZero = Cond.`when`(valueOf(AMOUNT_AVAILABLE_REF).equalToValue(delta))
            .then(true).otherwiseValueOf(SHOULD_BE_NOTIFIED_REF)
        return update()
            .set(SetOperation(AMOUNT_AVAILABLE, Add.valueOf(AMOUNT_AVAILABLE_REF).add(delta)))
            .set(SetOperation(SHOULD_BE_NOTIFIED, ifAmountWasZero))
    }

    private fun filterByIdAndValidAmountAvailable(bookId: String, delta: Int): Query {
        val query = whereId(bookId)
        if (delta < 0) {
            val positiveDelta = -delta
            query.addCriteria(where(AMOUNT_AVAILABLE).gte(positiveDelta))
        }
        return query
    }

    override fun updateShouldBeNotified(bookId: String, newValue: Boolean): Mono<Long> {
        val update = Update().set(SHOULD_BE_NOTIFIED, newValue)
        return mongoTemplate.updateFirst<MongoBook>(whereId(bookId), update)
            .map { it.modifiedCount }
    }

    override fun delete(id: String): Mono<Long> {
        return mongoTemplate.remove<MongoBook>(whereId(id))
            .map { it.deletedCount }
    }

    override fun update(id: String, request: UpdateBookRequest): Mono<DomainBook> {
        val op = options().returnNew(true)
        return mongoTemplate.findAndModify(whereId(id), request.toUpdate(), op, MongoBook::class.java)
            .map { mongoBook -> mongoBook.toDomain() }
    }

    private fun whereId(bookId: String) = query(where(Fields.UNDERSCORE_ID).isEqualTo(bookId))

    companion object {
        const val SHOULD_BE_NOTIFIED: String = "shouldBeNotified"
        const val SHOULD_BE_NOTIFIED_REF: String = "\$" + SHOULD_BE_NOTIFIED
        const val AMOUNT_AVAILABLE = "amountAvailable"
        const val AMOUNT_AVAILABLE_REF = "\$" + AMOUNT_AVAILABLE
    }
}
