package pet.project.app.repository.impl

import com.mongodb.ClientSessionOptions
import com.mongodb.client.ClientSession
import org.springframework.data.mongodb.core.BulkOperations
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
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.model.Book
import pet.project.app.repository.BookRepository

@Repository
@Profiling
internal class BookMongoRepository(private val mongoTemplate: MongoTemplate) : BookRepository {

    override fun insert(book: Book): Book {
        return mongoTemplate.insert(book)
    }

    override fun findById(id: String): Book? {
        return mongoTemplate.findById(id, Book::class.java)
    }

    override fun existsById(id: String): Boolean {
        return mongoTemplate.exists<Book>(whereId(id))
    }

    override fun updateAmount(request: UpdateAmountRequest): Boolean {
        val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
        val result = mongoTemplate.updateFirst<Book>(query, withAmountUpdatePipeline(request.delta))
        return result.modifiedCount == 1L
    }

    override fun updateAmountMany(requests: List<UpdateAmountRequest>): Int {
        val sessionOptions = ClientSessionOptions.builder().causallyConsistent(true).build()
        val session: ClientSession = mongoTemplate.mongoDatabaseFactory.getSession(sessionOptions)

        session.startTransaction()
        val bulkOps = mongoTemplate.withSession(session)
            .bulkOps(BulkOperations.BulkMode.ORDERED, Book::class.java)

        for (request in requests) {
            val query = filterByIdAndValidAmountAvailable(request.bookId, request.delta)
            bulkOps.updateOne(query, withAmountUpdatePipeline(request.delta))
        }

        val matchedCount = bulkOps.execute().matchedCount
        if (matchedCount != requests.size) session.abortTransaction() else session.commitTransaction()

        return matchedCount
    }

    private fun withAmountUpdatePipeline(delta: Int): AggregationUpdate {
        val ifAmountWasZero = Cond.`when`(valueOf("\$amountAvailable").equalToValue(delta))
            .then(true).otherwiseValueOf("\$shouldBeNotified")
        return update()
            .set(SetOperation("amountAvailable", Add.valueOf("\$amountAvailable").add(delta)))
            .set(SetOperation("shouldBeNotified", ifAmountWasZero))
    }

    private fun filterByIdAndValidAmountAvailable(bookId: String, delta: Int): Query {
        val query = whereId(bookId)
        if (delta < 0) {
            val positiveDelta = -delta
            query.addCriteria(where("amountAvailable").gte(positiveDelta))
        }
        return query
    }

    override fun updateShouldBeNotified(bookId: String, boolValue: Boolean): Long {
        val update = Update().set("shouldBeNotified", boolValue)
        return mongoTemplate.updateFirst<Book>(whereId(bookId), update).modifiedCount
    }

    override fun update(book: Book): Long {
        val filterById = query(where(Fields.UNDERSCORE_ID).isEqualTo(book.id))
        return mongoTemplate.replace(filterById, book).modifiedCount
    }

    override fun delete(id: String): Long {
        return mongoTemplate.remove<Book>(whereId(id)).deletedCount
    }

    private fun whereId(bookId: String) = query(where(Fields.UNDERSCORE_ID).isEqualTo(bookId))
}
