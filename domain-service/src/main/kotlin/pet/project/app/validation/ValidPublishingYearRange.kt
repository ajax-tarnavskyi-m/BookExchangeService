package pet.project.app.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pet.project.app.validation.validator.ValidPublishingYearRangeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPublishingYearRangeValidator::class])
annotation class ValidPublishingYearRange(
    val message: String = "Publishing year must be between 1600 and the current year",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
