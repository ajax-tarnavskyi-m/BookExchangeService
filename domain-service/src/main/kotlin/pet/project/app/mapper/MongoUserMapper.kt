package pet.project.app.mapper

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Update
import pet.project.app.model.domain.DomainUser
import pet.project.app.model.mongo.MongoUser
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserRequest.WishListUpdate

object MongoUserMapper {

    fun CreateUserRequest.toMongo() = MongoUser(
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
        if (login.isNotEmpty()) update.set(MongoUser::login.name, login)
        if (email.isNotEmpty()) update.set(MongoUser::email.name, email)
        if (hasBookWishList()) {
            update.set(MongoUser::bookWishList.name, bookWishList.toObjectIdSet())
        }
        return update
    }

    private fun WishListUpdate.toObjectIdSet() = bookIdsList
        .map { id -> ObjectId(id) }
        .toSet()
}
