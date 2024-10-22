package pet.project.core.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pet.project.core.validation.NotZero

class NotZeroValidator : ConstraintValidator<NotZero, Int> {
    override fun isValid(value: Int, context: ConstraintValidatorContext?): Boolean {
        return value != 0
    }
}
