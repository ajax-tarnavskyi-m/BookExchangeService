package pet.project.app.dto.book

import pet.project.app.model.Book

object BookMapper {

    fun Book.toDto() = ResponseBookDto(
        id,
        title ?: "",
        description,
        yearOfPublishing ?: 0,
        price,
        amountAvailable ?: 0
    )

    fun RequestBookDto.toModel() = Book(
        title,
        description,
        yearOfPublishing,
        price,
        amountAvailable
    )

}
