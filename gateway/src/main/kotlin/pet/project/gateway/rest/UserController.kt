package pet.project.gateway.rest

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pet.project.core.validation.ValidObjectId
import pet.project.gateway.client.NatsClient
import pet.project.gateway.dto.user.CreateUserExternalRequest
import pet.project.gateway.dto.user.UpdateUserExternalRequest
import pet.project.gateway.dto.user.UserExternalResponse
import pet.project.gateway.mapper.UserRequestMapper.toAddBookToUsersWishListRequest
import pet.project.gateway.mapper.UserRequestMapper.toDeleteUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toFindUserByIdRequest
import pet.project.gateway.mapper.UserRequestMapper.toProto
import pet.project.gateway.mapper.UserRequestMapper.toUpdateUserRequest
import pet.project.gateway.mapper.UserResponseMapper.handleResponse
import pet.project.gateway.mapper.UserResponseMapper.toExternal
import pet.project.gateway.mapper.UserResponseMapper.toExternalResponse
import pet.project.internal.app.subject.NatsSubject
import pet.project.internal.input.reqreply.user.AddBookToUsersWishListResponse
import pet.project.internal.input.reqreply.user.CreateUserResponse
import pet.project.internal.input.reqreply.user.DeleteUserByIdResponse
import pet.project.internal.input.reqreply.user.FindUserByIdResponse
import pet.project.internal.input.reqreply.user.UpdateUserResponse
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user")
class UserController(private val natsClient: NatsClient) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody createUserRequest: CreateUserExternalRequest): Mono<UserExternalResponse> {
        return natsClient.doRequest(
            NatsSubject.User.CREATE,
            createUserRequest.toProto(),
            CreateUserResponse.parser()
        ).map { it.toExternalResponse() }
    }

    @GetMapping("/{id}")
    fun getById(@ValidObjectId @PathVariable("id") userId: String): Mono<UserExternalResponse> {
        return natsClient.doRequest(
            NatsSubject.User.FIND_BY_ID,
            toFindUserByIdRequest(userId),
            FindUserByIdResponse.parser()
        ).map { it.toExternalResponse() }
    }

    @PutMapping("/{id}/wishlist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addBookToWishList(
        @ValidObjectId @PathVariable("id") userId: String,
        @ValidObjectId @RequestParam bookId: String,
    ): Mono<Unit> {
        return natsClient.doRequest(
            NatsSubject.User.ADD_BOOK_TO_WISH_LIST,
            toAddBookToUsersWishListRequest(userId, bookId),
            AddBookToUsersWishListResponse.parser()
        ).map { it.toExternal() }
    }

    @PutMapping("/{id}")
    fun update(
        @ValidObjectId @PathVariable("id") userId: String,
        @RequestBody @Valid request: UpdateUserExternalRequest,
    ): Mono<UserExternalResponse> {
        return natsClient.doRequest(
            NatsSubject.User.UPDATE,
            toUpdateUserRequest(userId, request),
            UpdateUserResponse.parser()
        ).map { it.toExternalResponse() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@ValidObjectId @PathVariable("id") userId: String): Mono<Unit> {
        return natsClient.doRequest(
            NatsSubject.User.DELETE,
            toDeleteUserByIdRequest(userId),
            DeleteUserByIdResponse.parser()
        ).map { it.handleResponse() }
    }
}
