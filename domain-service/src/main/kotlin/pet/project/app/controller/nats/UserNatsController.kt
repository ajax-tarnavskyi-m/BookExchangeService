package pet.project.app.controller.nats

import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pet.project.app.annotation.NatsController
import pet.project.app.annotation.NatsHandler
import pet.project.app.controller.nats.UserNatsController.Companion.QUEUE_GROUP
import pet.project.app.mapper.UserControllerMapper.toAddBookToUserWishListResponse
import pet.project.app.mapper.UserControllerMapper.toCreateUserResponse
import pet.project.app.mapper.UserControllerMapper.toDeleteUserByIdResponse
import pet.project.app.mapper.UserControllerMapper.toFindUserByIdResponse
import pet.project.app.mapper.UserControllerMapper.toUpdateUserResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureAddBookToUserWishListResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureCreateUserResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureDeleteUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureFindUserByIdResponse
import pet.project.app.mapper.UserThrowableMapper.toFailureUpdateUserResponse
import pet.project.app.service.UserService
import pet.project.internal.app.subject.UserNatsSubject
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListRequest
import pet.project.internal.input.reqreply.user.add_book_to_wish_list.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.create.CreateUserRequest
import pet.project.internal.input.reqreply.user.create.CreateUserResponse
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdRequest
import pet.project.internal.input.reqreply.user.delete.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.find.FindUserByIdRequest
import pet.project.internal.input.reqreply.user.find.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.update.UpdateUserRequest
import pet.project.internal.input.reqreply.user.update.UpdateUserResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
@NatsController(subjectPrefix = UserNatsSubject.PREFIX, queueGroup = QUEUE_GROUP)
class UserNatsController(
    private val userService: UserService,
    connection: Connection,
    dispatcher: Dispatcher,
) : AbstractNatsController(connection, dispatcher) {

    @NatsHandler(subject = UserNatsSubject.CREATE)
    fun create(request: CreateUserRequest): Mono<CreateUserResponse> {
        return userService.create(request)
            .map { it.toCreateUserResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureCreateUserResponse().toMono()
            }
    }

    @NatsHandler(subject = UserNatsSubject.FIND_BY_ID)
    fun getById(request: FindUserByIdRequest): Mono<FindUserByIdResponse> {
        return userService.getById(request.id)
            .map { it.toFindUserByIdResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureFindUserByIdResponse().toMono()
            }
    }

    @NatsHandler(subject = UserNatsSubject.ADD_BOOK_TO_WISH_LIST)
    fun addBookToWishList(request: AddBookToUsersWishListRequest): Mono<AddBookToUsersWishListResponse> {
        return userService.addBookToWishList(request.userId, request.bookId)
            .map { toAddBookToUserWishListResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureAddBookToUserWishListResponse().toMono()
            }
    }

    @NatsHandler(subject = UserNatsSubject.UPDATE)
    fun update(request: UpdateUserRequest): Mono<UpdateUserResponse> {
        return userService.update(request.id, request)
            .map { it.toUpdateUserResponse() }
            .onErrorResume { error ->
                log.error("Error while executing", error)
                error.toFailureUpdateUserResponse().toMono()
            }
    }

    @NatsHandler(subject = UserNatsSubject.DELETE)
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
