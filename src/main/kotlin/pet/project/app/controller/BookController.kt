package pet.project.app.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pet.project.app.model.Book
import pet.project.app.service.BookService

@RestController
@RequestMapping("/book")
class BookController(val bookService: BookService) {

    @PostMapping("/")
    fun create(book: Book) : Book{
        return bookService.create(book)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long): Book {
        return bookService.getById(id)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: Long, book: Book): Book {
        return bookService.update(id, book)
    }

    @PatchMapping("/{id}/increase-amount")
    fun increaseAmount(@PathVariable("id") id: Long, @RequestParam addition: Int = 1) : Int {
        return bookService.increaseAmount(id, addition)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: Long) : Book {
        return bookService.delete(id)
    }
}
