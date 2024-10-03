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
import pet.project.app.dto.book.BookMapper
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateAmountRequest
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.service.BookService
import pet.project.app.validation.ValidObjectId

@RestController
@RequestMapping("/book")
class BookController(private val bookService: BookService, private val mapper: BookMapper) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookRequest): ResponseBookDto {
        val createdBook = bookService.create(mapper.toModel(request))
        return mapper.toDto(createdBook)
    }

    @GetMapping("/{id}")
    fun getById(@ValidObjectId @PathVariable("id") id: String) = mapper.toDto(bookService.getById(id))

    @PutMapping("/")
    fun update(@Valid @RequestBody request: UpdateBookRequest): ResponseBookDto {
        val updatedBook = bookService.update(mapper.toModel(request))
        return mapper.toDto(updatedBook)
    }

    @PatchMapping("/amount")
    fun updateAmount(@Valid @RequestBody request: UpdateAmountRequest): Boolean {
        return bookService.updateAmount(request)
    }

    @PostMapping("/amount")
    fun exchangeBooks(@Valid @RequestBody requests: List<UpdateAmountRequest>): Boolean {
        return bookService.exchangeBooks(requests)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@ValidObjectId @PathVariable("id") id: String) = bookService.delete(id)
}
