package pet.project.app.dto.book

import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import pet.project.app.model.Book

@Component
class BookMapper {

    fun toDto(book: Book) = ResponseBookDto(
        book.id!!.toHexString(),
        book.title,
        book.description,
        book.yearOfPublishing ?: 0,
        book.price,
        book.amountAvailable,
    )

    fun toModel(request: CreateBookRequest) = Book(
        title = request.title,
        description = request.description,
        yearOfPublishing = request.yearOfPublishing,
        price = request.price,
        amountAvailable = request.amountAvailable,
    )

    fun toModel(request: UpdateBookRequest) = Book(
        ObjectId(request.id),
        request.title,
        request.description,
        request.yearOfPublishing,
        request.price,
        request.amountAvailable,
    )
}
