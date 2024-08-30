package pet.project.app.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pet.project.app.validation.YearBeforeCurrent
import java.time.Year

class YearBeforeValidator : ConstraintValidator<YearBeforeCurrent, Int> {
    override fun isValid(value: Int, context: ConstraintValidatorContext?): Boolean {
        return value <= Year.now().value
    }
}
