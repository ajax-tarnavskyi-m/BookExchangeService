package pet.project.app.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pet.project.app.validation.ValidPublishingYearRange
import java.time.Year

/**
 * A custom validator for the `@ValidPublishingYearRange` annotation.
 *
 * This validator is designed to ensure that the publishing year of a book falls within a valid range,
 * defined by business rules specific to the application's context. The range is set between a minimum year of 1600
 * and a maximum year that is five years beyond the current year. The decision to extend the range five years
 * into the future accommodates pre-publication records, advanced planning, and any business scenarios
 * where a book's publishing year might be projected.
 *
 * @see ValidPublishingYearRange
 */
class ValidPublishingYearRangeValidator : ConstraintValidator<ValidPublishingYearRange, Int> {

    override fun isValid(value: Int, context: ConstraintValidatorContext?): Boolean {
        val maxYear = Year.now().value + FUTURE_YEARS_ALLOWANCE
        return value in MIN_YEAR..maxYear
    }

    companion object {
        private const val FUTURE_YEARS_ALLOWANCE = 5
        private const val MIN_YEAR = 1600
    }
}
