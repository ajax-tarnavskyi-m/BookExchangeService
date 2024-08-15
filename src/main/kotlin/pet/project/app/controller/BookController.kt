package pet.project.app.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pet.project.app.controller.dto.book.BookMapper.toDto
import pet.project.app.controller.dto.book.BookMapper.toModel
import pet.project.app.controller.dto.book.RequestBookDto
import pet.project.app.controller.dto.book.ResponseBookDto
import pet.project.app.service.BookService

@RestController
@RequestMapping("/book")
class BookController(val bookService: BookService) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody bookDto: RequestBookDto): ResponseBookDto {
        val createdBook = bookService.create(bookDto.toModel())
        return createdBook.toDto()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): ResponseBookDto {
        return bookService.getById(id).toDto()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: Long, @RequestBody bookDto: RequestBookDto): ResponseBookDto {
        val updatedBook = bookService.update(id, bookDto.toModel())
        return updatedBook.toDto()
    }

    //TODO validate that 'addition' is positive
    @PatchMapping("/{id}/increase-amount")
    fun increaseAmount(@PathVariable("id") id: Long, @RequestParam addition: Int = 1): Int {
        return bookService.increaseAmount(id, addition)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("id") id: Long) {
        return bookService.delete(id)
    }
}
