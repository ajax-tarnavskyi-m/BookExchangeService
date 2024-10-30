package pet.project.app.mapper

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.query.Update
import pet.project.app.mapper.MongoUserMapper.toDomain
import pet.project.app.mapper.MongoUserMapper.toUpdate
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoUser
import pet.project.core.RandomTestFields.Book.randomBookId
import pet.project.core.RandomTestFields.Book.randomBookIdString
import pet.project.core.RandomTestFields.User.randomEmail
import pet.project.core.RandomTestFields.User.randomLogin
import pet.project.core.RandomTestFields.User.randomUserId
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate

class MongoUserMapperTest {

    @Test
    fun `should map all fields correctly in toDomain`() {
        // GIVEN
        val mongoUser = MongoUser(randomUserId(), randomLogin(), randomEmail(), setOf(randomBookId()))
        val expected = DomainUser(
            mongoUser.id!!.toHexString(),
            mongoUser.login!!,
            mongoUser.email!!,
            mongoUser.bookWishList!!.map(ObjectId::toHexString).toSet()
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
        val bookIdString = randomBookIdString()
        val updateUserRequest = UpdateUserRequest.newBuilder()
            .setLogin(randomLogin())
            .setEmail(randomEmail())
            .setBookWishList(WishListUpdate.newBuilder().addAllBookIds(setOf(bookIdString)))
            .build()
        val expected = Update()
            .set(MongoUser::login.name, updateUserRequest.login)
            .set(MongoUser::email.name, updateUserRequest.email)
            .set(MongoUser::bookWishList.name, setOf(ObjectId(bookIdString)))

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
