package pet.project.app.mapper

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoUser
import pet.project.internal.commonmodels.user.user.User
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest.WishListUpdate

@Component
object UserRepositoryMapper {

    fun CreateUserRequest.toMongo() = user.toMongo()

    fun User.toMongo() = MongoUser(
        login = login,
        email = email,
        bookWishList = bookWishListList.map { ObjectId(it) }.toSet()
    )

    fun MongoUser.toDomain() = DomainUser(
        id.toString(),
        login.orEmpty(),
        email.orEmpty(),
        bookWishList?.map { it.toHexString() }?.toSet().orEmpty()
    )

    fun UpdateUserRequest.toUpdate(): Update {
        val update = Update()
        if (login != "") update.set(MongoUser::login.name, login)
        if (email != "") update.set(MongoUser::email.name, email)
        if (hasBookWishList()) {
            update.set(MongoUser::bookWishList.name, bookWishList.toObjectIdSet())
        }
        return update
    }

    private fun WishListUpdate.toObjectIdSet() = bookIdsList
        .map { id -> ObjectId(id) }
        .toSet()
}
