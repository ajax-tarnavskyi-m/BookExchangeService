package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.query.Update
import pet.project.app.mapper.MongoUserMapper.toDomain
import pet.project.app.mapper.MongoUserMapper.toUpdate
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoUser
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate

class MongoUserMapperTest {

    @Test
    fun `should map all fields correctly in toDomain`() {
        // GIVEN
        val userId = ObjectId.get()
        val bookWishList = setOf(ObjectId.get(), ObjectId.get())
        val mongoUser = MongoUser(userId, "testUser", "test@example.com", bookWishList)
        val expected = DomainUser(
            userId.toHexString(),
            "testUser",
            "test@example.com",
            bookWishList.map { it.toHexString() }.toSet()
        )

        // WHEN
        val actual = mongoUser.toDomain()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should handle null values correctly in toDomain`() {
        // GIVEN
        val mongoUser = MongoUser()
        val expected = DomainUser("null", "", "", emptySet())

        // WHEN
        val actual = mongoUser.toDomain()

        // THEN
        assertEquals(expected, actual)
    }

    @Test
    fun `should set all fields correctly in toUpdate`() {
        // GIVEN
        val wishList = setOf(ObjectId.get())
        val updateUserRequest = UpdateUserRequest.newBuilder()
            .setLogin("newLogin")
            .setEmail("newEmail@example.com")
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(wishList.map { it.toHexString() }))
            .build()
        val expected = Update()
            .set(MongoUser::login.name, "newLogin")
            .set(MongoUser::email.name, "newEmail@example.com")
            .set(MongoUser::bookWishList.name, wishList)

        // WHEN
        val update = updateUserRequest.toUpdate()

        // THEN
        assertEquals(expected, update)
    }

    @Test
    fun `should not set fields when they are null in toUpdate`() {
        // GIVEN
        val updateUserRequest = UpdateUserRequest.newBuilder().build()

        // WHEN
        val resultUpdate = updateUserRequest.toUpdate()

        // THEN
        val emptyUpdate = Update()
        assertEquals(emptyUpdate, resultUpdate)
    }
}
