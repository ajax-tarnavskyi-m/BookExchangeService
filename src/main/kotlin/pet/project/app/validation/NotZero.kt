package pet.project.app.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pet.project.app.validation.validator.NotZeroValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotZeroValidator::class])
annotation class NotZero(
    val message: String = "Value must not be zero",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
