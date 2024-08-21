package pet.project.app.dto.book

import pet.project.app.model.Book

object BookMapper {

    fun Book.toDto() = ResponseBookDto(
        id!!,
        title ?: "",
        description,
        yearOfPublishing ?: 0,
        price,
        amountAvailable ?: 0
    )

    fun CreateBookRequest.toModel() = Book(
        title = title,
        description = description,
        yearOfPublishing = yearOfPublishing,
        price = price,
        amountAvailable = amountAvailable
    )

    fun UpdateBookRequest.toModel() = Book(
        id,
        title,
        description,
        yearOfPublishing,
        price,
        amountAvailable
    )

}
