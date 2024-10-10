package pet.project.app.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.indexOps
import pet.project.app.config.DatabaseChangeLog.Companion.INDEX_NAME
import pet.project.app.model.mongo.MongoUser
import pet.project.app.repository.AbstractMongoTestContainer

class DatabaseChangeLogTest : AbstractMongoTestContainer {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun `should create index with bookWishList list field and ASC direction`() {
        // WHEN
        DatabaseChangeLog().createIndexForUserWishlist(mongoTemplate)

        // THEN
        val foundIndex = mongoTemplate.indexOps<MongoUser>().indexInfo.first { it.name == INDEX_NAME }
        assertNotNull(foundIndex)
        val indexField = foundIndex.indexFields.first()
        assertEquals(MongoUser::bookWishList.name, indexField.key, "Index key should match MongoUser field name")
        assertEquals(Direction.ASC, indexField.direction, "Should create index with ASC direction")
    }

    @Test
    fun `should remove index created by migration and log about success`() {
        // GIVEN
        val dbChangeLog = DatabaseChangeLog().apply { createIndexForUserWishlist(mongoTemplate) }
        val indexInfo = mongoTemplate.indexOps<MongoUser>().indexInfo.filter { it.name == INDEX_NAME }
        assertTrue(indexInfo.isNotEmpty(), "Index should exist before rollback")

        val logger: Logger = LoggerFactory.getLogger(DatabaseChangeLog::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(listAppender)

        // WHEN
        dbChangeLog.rollbackIndexForUserWishlist(mongoTemplate)

        // THEN
        val indexInfoAfterRollback = mongoTemplate.indexOps<MongoUser>().indexInfo.filter { it.name == INDEX_NAME }
        assertTrue(indexInfoAfterRollback.isEmpty(), "No index with $INDEX_NAME should be found after rollback")

        val logs = listAppender.list
        val expected = "ROLLBACK: Index $INDEX_NAME of collection ${MongoUser.COLLECTION_NAME} successfully dropped"
        assertEquals(expected, logs.first().formattedMessage)
        assertEquals(Level.INFO, logs.first().level)
    }
}
