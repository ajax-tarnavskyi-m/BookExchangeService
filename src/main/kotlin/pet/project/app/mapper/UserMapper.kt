package pet.project.app.mapper

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoUser

@Component
object UserMapper {

    fun CreateUserRequest.toMongo() = MongoUser(
        login = login,
        email = email,
        bookWishList = bookWishList.map { ObjectId(it) }.toSet()
    )

    fun DomainUser.toDto() = ResponseUserDto(
        id,
        login,
        email,
        bookWishList,
    )

    fun MongoUser.toDomain() = DomainUser(
        id.toString(),
        login.orEmpty(),
        email.orEmpty(),
        bookWishList?.map { it.toHexString() }?.toSet().orEmpty()
    )

    fun UpdateUserRequest.toUpdate() : Update {
        val update = Update()
        login?.let { update.set(MongoUser::login.name, it) }
        email?.let { update.set(MongoUser::email.name, it) }
        bookWishList?.let { update.set(MongoUser::bookWishList.name, it.map { id -> ObjectId(id) }.toSet()) }
        return update
    }
}
