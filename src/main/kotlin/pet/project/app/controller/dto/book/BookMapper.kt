package pet.project.app.controller.dto.book

import pet.project.app.repository.model.Book

object BookMapper {

    fun Book.toDto(): ResponseBookDto {
        return ResponseBookDto(
            this.id,
            this.title,
            this.description,
            this.yearOfPublishing,
            this.amazonPrice,
            this.amountAvailable
        )
    }

    fun RequestBookDto.toModel(): Book {
        return Book(
            this.title,
            this.description,
            this.yearOfPublishing,
            this.amazonPrice,
            this.amountAvailable
        )
    }
}
