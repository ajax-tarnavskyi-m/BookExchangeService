package pet.project.app.repository.impl

import com.mongodb.ClientSessionOptions
import com.mongodb.client.ClientSession
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.MongoTemplate
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

@Repository
@Profiling
@Suppress("TooManyFunctions")
internal class BookMongoRepository(private val mongoTemplate: MongoTemplate) : BookRepository {

    override fun insert(createBookRequest: CreateBookRequest): DomainBook {
        return mongoTemplate.insert(createBookRequest.toMongo()).toDomain()
    }

    override fun findById(id: String): DomainBook? {
        return mongoTemplate.findById(id, MongoBook::class.java)?.toDomain()
    }

    override fun existsById(id: String): Boolean {
        return mongoTemplate.exists<MongoBook>(whereId(id))
    }

    override fun updateAmount(request: UpdateAmountRequest): Boolean {
        val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
        val result = mongoTemplate.updateFirst<MongoBook>(query, withAmountUpdatePipeline(request.delta))
        return result.modifiedCount == 1L
    }

    override fun updateAmountMany(requests: List<UpdateAmountRequest>): Int {
        val sessionOptions = ClientSessionOptions.builder().causallyConsistent(true).build()
        val session: ClientSession = mongoTemplate.mongoDatabaseFactory.getSession(sessionOptions)

        session.startTransaction()
        val bulkOps = mongoTemplate.withSession(session)
            .bulkOps(BulkOperations.BulkMode.ORDERED, MongoBook::class.java)

        for (request in requests) {
            val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
            bulkOps.updateOne(query, withAmountUpdatePipeline(request.delta))
        }

        val matchedCount = bulkOps.execute().matchedCount
        if (matchedCount != requests.size) session.abortTransaction() else session.commitTransaction()

        return matchedCount
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

    override fun updateShouldBeNotified(bookId: String, newValue: Boolean): Long {
        val update = Update().set(SHOULD_BE_NOTIFIED, newValue)
        return mongoTemplate.updateFirst<MongoBook>(whereId(bookId), update).modifiedCount
    }

    override fun delete(id: String): Long {
        return mongoTemplate.remove<MongoBook>(whereId(id)).deletedCount
    }

    override fun update(id: String, request: UpdateBookRequest): DomainBook? {
        val op = options().returnNew(true)
        return mongoTemplate.findAndModify(whereId(id), request.toUpdate(), op, MongoBook::class.java)?.toDomain()
    }

    private fun whereId(bookId: String) = query(where(Fields.UNDERSCORE_ID).isEqualTo(bookId))

    companion object {
        const val SHOULD_BE_NOTIFIED: String = "shouldBeNotified"
        const val SHOULD_BE_NOTIFIED_REF: String = "\$" + SHOULD_BE_NOTIFIED
        const val AMOUNT_AVAILABLE = "amountAvailable"
        const val AMOUNT_AVAILABLE_REF = "\$" + AMOUNT_AVAILABLE
    }
}
