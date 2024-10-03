package pet.project.app.config

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.DEFAULT_DIRECTION
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import pet.project.app.model.User


@ChangeUnit(id = "createUserWishlistIndex", order = "001", author = "ajax-tarnavskyi-m")
class DatabaseChangeLog {

    @Execution
    fun createIndexForUserWishlist(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps(User.COLLECTION_NAME)
            .ensureIndex(Index(User::bookWishList.name, DEFAULT_DIRECTION).named(INDEX_NAME))
    }

    @RollbackExecution
    fun rollbackIndexForUserWishlist(mongoTemplate: MongoTemplate) {
        val indexExists = mongoTemplate.getCollection(User.COLLECTION_NAME).listIndexes()
            .any { it.getString("name") == INDEX_NAME }
        if (indexExists) {
            mongoTemplate.indexOps(User.COLLECTION_NAME).dropIndex(INDEX_NAME)
            log.info("ROLLBACK: Index {} of collection {} successfully dropped", INDEX_NAME, User.COLLECTION_NAME)
        } else {
            log.info("ROLLBACK: Index {} of collection {} is not found", INDEX_NAME, User.COLLECTION_NAME)
        }
    }

    companion object {
        const val INDEX_NAME = "bookWishList_index"
        private val log = LoggerFactory.getLogger(DatabaseChangeLog::class.java)
    }
}
