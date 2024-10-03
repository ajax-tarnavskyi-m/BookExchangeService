package pet.project.app.dto.book

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.dto.user.UserMapper
import pet.project.app.model.User

class UserMapperTest {

    private val mapper = UserMapper()

    @Test
    fun `toModel should map CreateUserRequest to User correctly`() {
        // GIVEN
        val request = CreateUserRequest(
            "testLogin",
            "test.user@example.com",
            setOf("60f1b13e8f1b2c000b355777", "60f1b13e8f1b2c000b355778"),
        )

        val expectedUser = User(
            null,
            "testLogin",
            "test.user@example.com",
            setOf(ObjectId("60f1b13e8f1b2c000b355777"), ObjectId("60f1b13e8f1b2c000b355778"))
        )

        // WHEN
        val actualUser = mapper.toModel(request)

        // THEN
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun `toModel should map UpdateUserRequest to User correctly`() {
        // GIVEN
        val request = UpdateUserRequest(
            "507f1f77bcf86cd799439011",
            "updatedLogin",
            "test.user@example.com",
            setOf("60f1b13e8f1b2c000b355779")
        )

        val expectedUser = User(
            ObjectId("507f1f77bcf86cd799439011"),
            "updatedLogin",
            "test.user@example.com",
            setOf(ObjectId("60f1b13e8f1b2c000b355779"))
        )

        // WHEN
        val actualUser = mapper.toModel(request)

        // THEN
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun `toDto should map User to ResponseUserDto correctly`() {
        // GIVEN
        val user = User(
            ObjectId("507f1f77bcf86cd799439011"),
            "testLogin",
            "test.user@example.com",
            setOf(ObjectId("60f1b13e8f1b2c000b355777"), ObjectId("60f1b13e8f1b2c000b355778"))
        )

        val expectedDto = ResponseUserDto(
            "507f1f77bcf86cd799439011",
            "testLogin",
            "test.user@example.com",
            setOf("60f1b13e8f1b2c000b355777", "60f1b13e8f1b2c000b355778")
        )

        // WHEN
        val actualDto = mapper.toDto(user)

        // THEN
        assertEquals(expectedDto, actualDto)
    }
}
