package pet.project.app.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pet.project.app.validation.NotZero

class NotZeroValidator : ConstraintValidator<NotZero, Int> {
    override fun isValid(value: Int, context: ConstraintValidatorContext?): Boolean {
        return value != 0
    }
}
