package pet.project.app.exception.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.HandlerMethodValidationException
import pet.project.app.exception.BookNotFoundException
import pet.project.app.exception.UserNotFoundException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(
        value = [BookNotFoundException::class, UserNotFoundException::class, IllegalArgumentException::class]
    )
    fun handle(ex: RuntimeException): ResponseEntity<Any> {
        return toResponseEntity(getTimestamp(), ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val reports = ex.bindingResult.fieldErrors.map {
            InvalidInputReport(it.field, it.defaultMessage)
        }
        return ResponseEntity(ValidationExceptionResponse(getTimestamp(), reports), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handle(ex: HandlerMethodValidationException): ResponseEntity<Any> {
        val message = ex.allValidationResults.first().resolvableErrors.first().defaultMessage
        return toResponseEntity(getTimestamp(), message, HttpStatus.BAD_REQUEST)
    }

    private fun toResponseEntity(timestamp: String, message: String?, httpStatus: HttpStatus): ResponseEntity<Any> {
        return ResponseEntity(ExceptionResponse(timestamp, message, httpStatus.value()), httpStatus)
    }

    private fun getTimestamp(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

data class ExceptionResponse(
    val timestamp: String,
    val message: String?,
    val status: Int,
)

data class ValidationExceptionResponse(
    val timestamp: String,
    val invalidInputReports: List<InvalidInputReport>,
    val status: Int = HttpStatus.BAD_REQUEST.value(),
)

data class InvalidInputReport(
    val field: String,
    val message: String?,
)
