package machinehead.pushitweb.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.ZonedDateTime

@ControllerAdvice
open class ApiResponseControllerAdvice {

    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    fun handleIllegalRequestException(exception: Exception): ResponseEntity<ApiError> {
        return ResponseEntity
            .badRequest()
            .body(exception.message?.let { ApiError(it, ZonedDateTime.now()) });
    }
}