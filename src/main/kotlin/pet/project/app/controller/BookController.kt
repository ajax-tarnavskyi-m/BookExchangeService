package pet.project.app.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.mapper.BookMapper.toDto
import pet.project.app.service.BookService
import pet.project.app.validation.ValidObjectId

@RestController
@RequestMapping("/book")
class BookController(private val bookService: BookService) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookRequest): ResponseBookDto {
        return bookService.create(request).toDto()
    }

    @GetMapping("/{id}")
    fun getById(@ValidObjectId @PathVariable("id") id: String) = bookService.getById(id).toDto()

    @PutMapping("/{id}")
    fun update(
        @ValidObjectId @PathVariable("id") id: String,
        @Valid @RequestBody request: UpdateBookRequest,
    ): ResponseBookDto {
        return bookService.update(id, request).toDto()
    }

    @PatchMapping("/amount")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAmount(@Valid @RequestBody request: UpdateAmountRequest) {
        bookService.updateAmount(request)
    }

    @PostMapping("/amount")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun exchangeBooks(@Valid @RequestBody requests: List<UpdateAmountRequest>) {
        bookService.exchangeBooks(requests)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@ValidObjectId @PathVariable("id") id: String) {
        bookService.delete(id)
    }
}

