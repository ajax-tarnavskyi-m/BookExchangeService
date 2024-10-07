package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.query.Update
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.mapper.UserMapper.toDomain
import pet.project.app.mapper.UserMapper.toUpdate
import pet.project.app.model.mongo.MongoUser

class UserMapperTest {

    @Test
    fun `toDomain should map all fields correctly`() {
        // GIVEN
        val bookWishList = setOf(ObjectId.get(), ObjectId.get())
        val mongoUser = MongoUser(
            id = ObjectId.get(),
            login = "testUser",
            email = "test@example.com",
            bookWishList = bookWishList
        )

        // WHEN
        val domainUser = mongoUser.toDomain()

        // THEN
        assertEquals(mongoUser.id.toString(), domainUser.id)
        assertEquals(mongoUser.login, domainUser.login)
        assertEquals(mongoUser.email, domainUser.email)
        assertEquals(bookWishList.map { it.toHexString() }.toSet(), domainUser.bookWishList)
    }

    @Test
    fun `toDomain should handle null values correctly`() {
        // GIVEN
        val mongoUser = MongoUser()

        // WHEN
        val domainUser = mongoUser.toDomain()

        // THEN
        assertEquals("", domainUser.login)
        assertEquals("", domainUser.email)
        assertEquals(emptySet<String>(), domainUser.bookWishList)
    }

    @Test
    fun `toUpdate should set all fields correctly`() {
        // GIVEN
        val updateUserRequest = UpdateUserRequest(
            login = "newLogin",
            email = "newEmail@example.com",
            bookWishList = setOf("5f8d0d55b54764421b7156c7", "5f8d0d55b54764421b7156c8")
        )

        // WHEN
        val update = updateUserRequest.toUpdate()

        // THEN
        assertTrue(update.modifies(MongoUser::login.name))
        assertTrue(update.modifies(MongoUser::email.name))
        assertTrue(update.modifies(MongoUser::bookWishList.name))
    }

    @Test
    fun `toUpdate should not set fields when they are null`() {
        // GIVEN
        val updateUserRequest = UpdateUserRequest(
            login = null,
            email = null,
            bookWishList = null
        )

        // WHEN
        val resultUpdate = updateUserRequest.toUpdate()

        // THEN
        val emptyUpdate = Update()
        assertEquals(emptyUpdate, resultUpdate)
    }
}
