package pet.project.gateway.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.bson.types.ObjectId
import pet.project.gateway.validation.ValidObjectId

class ValidObjectIdValidator : ConstraintValidator<ValidObjectId, String> {
    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return ObjectId.isValid(value)
    }
}
