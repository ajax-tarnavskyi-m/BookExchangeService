package pet.project.app.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pet.project.app.model.User
import pet.project.app.service.UserService

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {
    @PostMapping
    fun create(user: User) : User {
        return userService.create(user)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long) : User {
        return userService.getById(id)
    }

    @PutMapping("/{id}")
    fun update(id: Long, user: User) : User {
        return userService.update(id, user)
    }

    @DeleteMapping("/{id}")
    fun delete(id: Long ) : User{
        return userService.delete(id)
    }
}