package pet.project.app.exception.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val reports = ex.bindingResult.fieldErrors.map {
            InvalidInputReport(it.field, it.defaultMessage ?: "Invalid method argument")
        }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return ResponseEntity(ValidationExceptionResponse(timestamp, reports), HttpStatus.BAD_REQUEST)
    }
}

data class ValidationExceptionResponse(
    val timestamp: String,
    val invalidInputReports: List<InvalidInputReport>,
    val status: Int = HttpStatus.BAD_REQUEST.value(),
)

data class InvalidInputReport(
    val field: String,
    val message: String,
)
