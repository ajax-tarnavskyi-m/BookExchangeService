package pet.project.app.controller

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
import pet.project.app.dto.user.CreateUserRequest
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UpdateUserRequest
import pet.project.app.mapper.UserMapper.toDto
import pet.project.app.service.UserService
import pet.project.app.validation.ValidObjectId
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): Mono<ResponseUserDto> {
        return userService.create(request).map { user -> user.toDto() }
    }

    @GetMapping("/{id}")
    fun getById(@ValidObjectId @PathVariable("id") userId: String): Mono<ResponseUserDto> {
        return userService.getById(userId).map { user -> user.toDto() }
    }

    @PutMapping("/{id}/wishlist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addBookToWishList(
        @ValidObjectId @PathVariable("id") userId: String,
        @ValidObjectId @RequestParam bookId: String,
    ): Mono<Unit> {
        return userService.addBookToWishList(userId, bookId)
    }

    @PutMapping("/{id}")
    fun update(
        @ValidObjectId @PathVariable("id") userId: String,
        @RequestBody @Valid request: UpdateUserRequest,
    ): Mono<ResponseUserDto> {
        return userService.update(userId, request)
            .map { user -> user.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@ValidObjectId @PathVariable("id") userId: String): Mono<Unit> {
        return userService.delete(userId)
    }
}
