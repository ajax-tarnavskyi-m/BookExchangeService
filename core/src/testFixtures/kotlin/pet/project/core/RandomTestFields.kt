package pet.project.core

import io.github.serpro69.kfaker.Faker
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

object RandomTestFields {
    object User {
        fun randomUserId(): ObjectId = ObjectId.get()
        fun randomUserIdString(): String = ObjectId.get().toHexString()
        fun randomLogin() = Faker().name.nameWithMiddle()
        fun randomEmail() = Faker().internet.safeEmail()
    }

    object Book {
        fun randomBookId(): ObjectId = ObjectId.get()
        fun randomBookIdString(): String = ObjectId.get().toHexString()
        fun randomTitle() = Faker().book.title()
        fun randomDescription() = Faker().lorem.punctuation()
        fun randomYearOfPublishing() = Random.nextInt(1800, LocalDate.now().year)
        fun randomPrice() = BigDecimal(Random.nextDouble(5.0, 150.0))
        fun randomAmountAvailable() = Random.nextInt(1, 20)
    }
}
