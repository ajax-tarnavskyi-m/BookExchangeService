package pet.project.app.controller

import org.springframework.web.bind.annotation.*
import pet.project.app.controller.dto.user.RequestUserDto
import pet.project.app.controller.dto.user.ResponseUserDto
import pet.project.app.controller.dto.user.UserMapper.toDto
import pet.project.app.controller.dto.user.UserMapper.toModel
import pet.project.app.service.UserService

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {
    @PostMapping("/")
    fun create(@RequestBody userDto: RequestUserDto): ResponseUserDto {
        val createdUser = userService.create(userDto.toModel())
        return createdUser.toDto()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") userId: Long): ResponseUserDto {
        return userService.getById(userId).toDto()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") userId: Long, @RequestBody userDto: RequestUserDto): ResponseUserDto {
        val updatedUser = userService.update(userId, userDto.toModel())
        return updatedUser.toDto()
    }

    @PatchMapping("/{id}/wishlist")
    fun addBookToWishList(@PathVariable("id") userId: Long, @RequestParam bookId: Long) : Boolean {
        return userService.addBookToWishList(userId, bookId)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") userId: Long) {
        return userService.delete(userId)
    }
}