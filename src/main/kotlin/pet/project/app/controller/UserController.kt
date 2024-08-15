package pet.project.app.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pet.project.app.dto.user.RequestUserDto
import pet.project.app.dto.user.ResponseUserDto
import pet.project.app.dto.user.UserMapper.toDto
import pet.project.app.dto.user.UserMapper.toModel
import pet.project.app.service.UserService

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody userDto: RequestUserDto): ResponseUserDto {
        val createdUser = userService.create(userDto.toModel())
        return createdUser.toDto()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") userId: String): ResponseUserDto {
        return userService.getById(userId).toDto()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") userId: String, @RequestBody userDto: RequestUserDto): ResponseUserDto {
        val updatedUser = userService.update(userId, userDto.toModel())
        return updatedUser.toDto()
    }

    @PatchMapping("/{id}/wishlist")
    fun addBookToWishList(@PathVariable("id") userId: String, @RequestParam bookId: String) : Boolean {
        return userService.addBookToWishList(userId, bookId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("id") userId: String) {
        return userService.delete(userId)
    }
}