package pet.project.app.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pet.project.app.validation.validator.ValidObjectIdValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidObjectIdValidator::class])
annotation class ValidObjectId(
    val message: String = "The provided ID must be a valid ObjectId hex String",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
