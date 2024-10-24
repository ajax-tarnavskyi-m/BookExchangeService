package pet.project.app.config

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.DEFAULT_DIRECTION
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import pet.project.app.model.mongo.MongoUser

@ChangeUnit(id = "createUserWishlistIndex", order = "001", author = "ajax-tarnavskyi-m")
class DatabaseChangeLog {

    @Execution
    fun createIndexForUserWishlist(mongoTemplate: MongoTemplate) {
        mongoTemplate.indexOps<MongoUser>()
            .ensureIndex(Index(MongoUser::bookWishList.name, DEFAULT_DIRECTION).named(INDEX_NAME))
    }

    @RollbackExecution
    fun rollbackIndexForUserWishlist(mongoTemplate: MongoTemplate) {
        val indexExists = mongoTemplate.indexOps<MongoUser>().indexInfo
            .map { it.name }
            .any { it == INDEX_NAME }
        if (indexExists) {
            mongoTemplate.indexOps<MongoUser>().dropIndex(INDEX_NAME)
            log.info("ROLLBACK: Index {} of collection {} successfully dropped", INDEX_NAME, MongoUser.COLLECTION_NAME)
        } else {
            log.info("ROLLBACK: Index {} of collection {} is not found", INDEX_NAME, MongoUser.COLLECTION_NAME)
        }
    }

    companion object {
        const val INDEX_NAME = "bookWishList_index"
        private val log = LoggerFactory.getLogger(DatabaseChangeLog::class.java)
    }
}
