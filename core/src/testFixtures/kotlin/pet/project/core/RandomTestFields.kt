package pet.project.core

import io.github.serpro69.kfaker.Faker
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

object RandomTestFields {
    object User {
        val userId: ObjectId = ObjectId.get()
        val userIdString : String = userId.toHexString()
        val login = Faker().name.nameWithMiddle()
        val email = Faker().internet.safeEmail()
    }

    object SecondUser {
        val secondLogin = Faker().name.nameWithMiddle()
        val secondEmail = Faker().internet.safeEmail()
    }

    object Book {
        val bookId: ObjectId = ObjectId.get()
        val bookIdString: String = bookId.toHexString()

        val title = Faker().book.title()
        val description = Faker().lorem.punctuation()
        val yearOfPublishing = Random.nextInt(1800, LocalDate.now().year)
        val price = BigDecimal(Random.nextDouble(5.0, 150.0))
        val amountAvailable = Random.nextInt(1, 20)
    }

    object SecondBook {
        val secondTitle = Faker().book.title()
        val secondDescription = Faker().lorem.punctuation()
        val secondYearOfPublishing = Random.nextInt(1800, LocalDate.now().year)
        val secondPrice = BigDecimal(Random.nextDouble(5.0, 150.0))
        val secondAmountAvailable = Random.nextInt(1, 20)
    }

    object ThirdBook {
        val thirdTitle = Faker().book.title()
        val thirdDescription = Faker().lorem.punctuation()
        val thirdYearOfPublishing = Random.nextInt(1800, LocalDate.now().year)
        val thirdPrice = BigDecimal(Random.nextDouble(5.0, 150.0))
        val thirdAmountAvailable = Random.nextInt(1, 20)
    }
}
