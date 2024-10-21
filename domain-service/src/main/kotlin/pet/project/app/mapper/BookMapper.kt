package pet.project.app.mapper

import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import pet.project.app.dto.book.CreateBookRequest
import pet.project.app.dto.book.ResponseBookDto
import pet.project.app.dto.book.UpdateBookRequest
import pet.project.app.model.domain.DomainBook
import pet.project.app.model.mongo.MongoBook
import java.math.BigDecimal

@Component
object BookMapper {

    fun DomainBook.toDto() = ResponseBookDto(
        id,
        title,
        description,
        yearOfPublishing,
        price,
        amountAvailable,
    )

    fun CreateBookRequest.toMongo() = MongoBook(
        title = title,
        description = description,
        yearOfPublishing = yearOfPublishing,
        price = price,
        amountAvailable = amountAvailable,
        shouldBeNotified = false
    )

    fun MongoBook.toDomain() = DomainBook(
        id.toString(),
        title.orEmpty(),
        description.orEmpty(),
        yearOfPublishing ?: 0,
        price ?: BigDecimal.ZERO,
        amountAvailable ?: 0
    )

    fun UpdateBookRequest.toUpdate() : Update {
        val update = Update()
        title?.let { update.set(MongoBook::title.name, it) }
        description?.let { update.set(MongoBook::description.name, it) }
        yearOfPublishing?.let { update.set(MongoBook::yearOfPublishing.name, it) }
        price?.let { update.set(MongoBook::price.name, it) }
        return update
    }
}
