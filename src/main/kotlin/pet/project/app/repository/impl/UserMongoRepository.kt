package pet.project.app.repository.impl

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In
import org.springframework.data.mongodb.core.aggregation.BooleanOperators.And.and
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators.Eq
import org.springframework.data.mongodb.core.aggregation.EvaluationOperators.Expr
import org.springframework.data.mongodb.core.aggregation.Fields.field
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let.ExpressionVariable.newVariable
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.updateFirst
import org.springframework.stereotype.Repository
import pet.project.app.annotation.Profiling
import pet.project.app.dto.user.UserNotificationDetails
import pet.project.app.model.User
import pet.project.app.repository.UserRepository

@Repository
@Profiling
class UserMongoRepository(private val mongoTemplate: MongoTemplate) : UserRepository {

    override fun insert(user: User): User {
        return mongoTemplate.insert(user)
    }

    override fun findByIdOrNull(id: String): User? {
        return mongoTemplate.findById(id, User::class.java)
    }

    override fun update(user: User): Long {
        val updateResult = mongoTemplate.replace(query(where("_id").isEqualTo(user.id)), user)
        return updateResult.modifiedCount
    }

    override fun findAllBookSubscribers(bookId: String): List<UserNotificationDetails> {
        val newAggregation = newAggregation(
            User::class.java,
            match(where("bookWishList").`is`(ObjectId(bookId))),
            LookupOperation(
                "book",
                null,
                AggregationPipeline.of(match(Expr.valueOf(Eq.valueOf("\$_id").equalToValue(ObjectId(bookId))))),
                field("bookDetails")
            ),
            project("login", "email").and("bookDetails.title").`as`("bookTitles")
        )

        return mongoTemplate.aggregate(newAggregation, User::class.java, UserNotificationDetails::class.java)
            .mappedResults
    }

    override fun findAllBookListSubscribers(booksIds: List<String>): List<UserNotificationDetails> {
        val bookObjectIds = booksIds.map { ObjectId(it) }
        val let = Let.just(newVariable("wishListBook").forField("bookWishList"))
        val newAggregation = newAggregation(
            User::class.java,
            match(where("bookWishList").`in`(bookObjectIds)),
            LookupOperation(
                "book",
                let,
                AggregationPipeline.of(matchIdContainsIn(bookObjectIds)),
                field("bookDetails")
            ),
            project("login", "email").and("bookDetails.title").`as`("bookTitles")
        )

        return mongoTemplate.aggregate(newAggregation, User::class.java, UserNotificationDetails::class.java)
            .mappedResults
    }

    private fun matchIdContainsIn(bookIds: List<ObjectId?>): AggregationOperation {
        return match(
            Expr.valueOf(
                and(
                    In.arrayOf("\$\$wishListBook").containsValue("\$_id"),
                    In.arrayOf(bookIds).containsValue("\$_id")
                )
            )
        )
    }

    override fun addBookToWishList(userId: String, bookId: String): Long {
        val query = query(where("_id").isEqualTo(userId))
        return mongoTemplate.updateFirst<User>(query, Update().addToSet("bookWishList", ObjectId(bookId))).matchedCount
    }

    override fun delete(id: String): Long {
        val deleteResult = mongoTemplate.remove<User>(query(where("_id").isEqualTo(id)))
        return deleteResult.deletedCount
    }

}



