package pet.project.app.validation

import jakarta.validation.Constraint
import pet.project.app.validation.validator.YearBeforeValidator

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [YearBeforeValidator::class])
annotation class YearBeforeCurrent
