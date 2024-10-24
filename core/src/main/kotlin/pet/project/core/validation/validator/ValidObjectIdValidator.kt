package pet.project.core.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.bson.types.ObjectId
import pet.project.core.validation.ValidObjectId

class ValidObjectIdValidator : ConstraintValidator<ValidObjectId, String> {
    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return ObjectId.isValid(value)
    }
}
