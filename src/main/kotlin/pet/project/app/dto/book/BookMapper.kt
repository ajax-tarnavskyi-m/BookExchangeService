package pet.project.app.dto.book

import pet.project.app.exception.MappingNullValueException
import pet.project.app.model.Book

object BookMapper {

    fun Book.toDto() = ResponseBookDto(
        id,
        title ?: throw MappingNullValueException("title", "Book"),
        description,
        yearOfPublishing ?: throw MappingNullValueException("yearOfPublishing", "Book"),
        amazonPrice,
        amountAvailable ?: throw MappingNullValueException("amountAvailable", "Book")
    )


    fun RequestBookDto.toModel() = Book(
        title,
        description,
        yearOfPublishing,
        amazonPrice,
        amountAvailable
    )

}
