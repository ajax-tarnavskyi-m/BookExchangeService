package pet.project.app.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pet.project.app.validation.ValidOpenId

class OpenIdValidator : ConstraintValidator<ValidOpenId, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return false
        val regex = """^(https?|ftp)://[^\s/$.?#].[^\s]*${'$'}""".toRegex()
        return regex.matches(value)
    }
}
