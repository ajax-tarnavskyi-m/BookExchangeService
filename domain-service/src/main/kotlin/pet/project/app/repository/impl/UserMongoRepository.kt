package pet.project.app.repository.impl

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions.options
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In
import org.springframework.data.mongodb.core.aggregation.BooleanOperators.And.and
import org.springframework.data.mongodb.core.aggregation.EvaluationOperators.Expr
import org.springframework.data.mongodb.core.aggregation.Fields
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
import pet.project.app.mapper.UserRepositoryMapper.toDomain
import pet.project.app.mapper.UserRepositoryMapper.toMongo
import pet.project.app.mapper.UserRepositoryMapper.toUpdate
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoBook
import pet.project.app.model.mongo.MongoUser
import pet.project.app.repository.UserRepository
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
@Profiling
internal class UserMongoRepository(private val mongoTemplate: ReactiveMongoTemplate) : UserRepository {

    override fun insert(createUserRequest: CreateUserRequest): Mono<DomainUser> {
        return mongoTemplate.insert(createUserRequest.toMongo())
            .map { mongoUser -> mongoUser.toDomain() }
    }

    override fun findById(id: String): Mono<DomainUser> {
        return mongoTemplate.findById(id, MongoUser::class.java)
            .map { mongoUser -> mongoUser.toDomain() }
    }

    override fun findAllSubscribersOf(booksIds: List<String>): Flux<UserNotificationDetails> {
        val bookObjectIds = booksIds.map { ObjectId(it) }
        val let = Let.just(newVariable(WISHLIST_BOOK).forField(MongoUser::bookWishList.name))
        val newAggregation = newAggregation(
            MongoUser::class.java,
            match(where(MongoUser::bookWishList.name).`in`(bookObjectIds)),
            LookupOperation(
                MongoBook.COLLECTION_NAME,
                let,
                AggregationPipeline.of(matchIdContainsIn(bookObjectIds)),
                field(BOOK_DETAILS)
            ),
            project(MongoUser::login.name, MongoUser::email.name).and(BOOK_DETAILS_TITLE).`as`(BOOK_TITLES)
        )

        return mongoTemplate.aggregate(newAggregation, MongoUser::class.java, UserNotificationDetails::class.java)
    }

    private fun matchIdContainsIn(bookIds: List<ObjectId?>): AggregationOperation {
        return match(
            Expr.valueOf(
                and(
                    In.arrayOf(WISHLIST_BOOK_REF_REF).containsValue(Fields.UNDERSCORE_ID_REF),
                    In.arrayOf(bookIds).containsValue(Fields.UNDERSCORE_ID_REF)
                )
            )
        )
    }

    override fun addBookToWishList(userId: String, bookId: String): Mono<Long> {
        val update = Update().addToSet(MongoUser::bookWishList.name, ObjectId(bookId))
        return mongoTemplate.updateFirst<MongoUser>(whereId(userId), update)
            .map { it.matchedCount }
    }

    override fun update(userId: String, request: UpdateUserRequest): Mono<DomainUser> {
        val op = options().returnNew(true)
        return mongoTemplate.findAndModify(whereId(userId), request.toUpdate(), op, MongoUser::class.java)
            .map { mongoUser -> mongoUser.toDomain() }
    }

    override fun delete(id: String): Mono<Long> {
        return mongoTemplate.remove<MongoUser>(whereId(id))
            .map { it.deletedCount }
    }

    private infix fun whereId(userId: String) = query(where(Fields.UNDERSCORE_ID).isEqualTo(userId))

    companion object {
        const val BOOK_DETAILS = "bookDetails"
        const val BOOK_DETAILS_TITLE = "bookDetails.title"
        const val BOOK_TITLES = "bookTitles"
        const val WISHLIST_BOOK = "wishListBook"
        const val WISHLIST_BOOK_REF_REF = "\$\$" + WISHLIST_BOOK
    }
}
