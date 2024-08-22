package pet.project.app.dto.book

import org.bson.types.ObjectId
import pet.project.app.model.Book

object BookMapper {

    fun Book.toDto() = ResponseBookDto(
        id!!.toHexString(),
        title ?: "",
        description,
        yearOfPublishing ?: 0,
        price,
        amountAvailable ?: 0,
    )

    fun CreateBookRequest.toModel() = Book(
        title = title,
        description = description,
        yearOfPublishing = yearOfPublishing,
        price = price,
        amountAvailable = amountAvailable,
    )

    fun UpdateBookRequest.toModel() = Book(
        ObjectId(id),
        title,
        description,
        yearOfPublishing,
        price,
        amountAvailable,
    )

}
