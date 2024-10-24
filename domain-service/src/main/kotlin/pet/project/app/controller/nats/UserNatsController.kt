package pet.project.app.controller.nats

import org.slf4j.LoggerFactory
import pet.project.app.annotation.NatsController
import pet.project.app.annotation.NatsHandler
import pet.project.app.controller.nats.UserNatsController.Companion.QUEUE_GROUP
import pet.project.app.mapper.UserResponseMapper.toAddBookToUserWishListResponse
import pet.project.app.mapper.UserResponseMapper.toCreateUserResponse
import pet.project.app.mapper.UserResponseMapper.toDeleteUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserResponseMapper.toUpdateUserResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureAddBookToUserWishListResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureCreateUserResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureDeleteUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureFindUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureUpdateUserResponse
import pet.project.app.service.UserService
import pet.project.internal.app.subject.NatsSubject
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserRequest
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserRequest
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@NatsController(queueGroup = QUEUE_GROUP)
class UserNatsController(private val userService: UserService) {

    @NatsHandler(subject = NatsSubject.User.CREATE)
    fun create(request: CreateUserRequest): Mono<CreateUserResponse> {
        return userService.create(request)
            .map { it.toCreateUserResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureCreateUserResponse().toMono()
            }
    }

    @NatsHandler(subject = NatsSubject.User.FIND_BY_ID)
    fun getById(request: FindUserByIdRequest): Mono<FindUserByIdResponse> {
        return userService.getById(request.id)
            .map { it.toFindUserByIdResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureFindUserByIdResponse().toMono()
            }
    }

    @NatsHandler(subject = NatsSubject.User.ADD_BOOK_TO_WISH_LIST)
    fun addBookToWishList(request: AddBookToUsersWishListRequest): Mono<AddBookToUsersWishListResponse> {
        return userService.addBookToWishList(request.userId, request.bookId)
            .map { toAddBookToUserWishListResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureAddBookToUserWishListResponse().toMono()
            }
    }

    @NatsHandler(subject = NatsSubject.User.UPDATE)
    fun update(request: UpdateUserRequest): Mono<UpdateUserResponse> {
        return userService.update(request.id, request)
            .map { it.toUpdateUserResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureUpdateUserResponse().toMono()
            }
    }

    @NatsHandler(subject = NatsSubject.User.DELETE)
    fun delete(request: DeleteUserByIdRequest): Mono<DeleteUserByIdResponse> {
        return userService.delete(request.id)
            .map { toDeleteUserByIdResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureDeleteUserByIdResponse().toMono()
            }
    }

    companion object {
        const val QUEUE_GROUP = "user_group"
        private val log = LoggerFactory.getLogger(UserNatsController::class.java)
    }
}
