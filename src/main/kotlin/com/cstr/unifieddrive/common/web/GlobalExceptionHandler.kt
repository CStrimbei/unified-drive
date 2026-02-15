package com.cstr.unifieddrive.common.web

import com.cstr.unifieddrive.fs.exception.FsNodeNotFoundException
import com.cstr.unifieddrive.fs.exception.InvalidFsOperationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val error: String,
    val message: String?
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(FsNodeNotFoundException::class)
    fun handleFsNodeNotFound(ex: FsNodeNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    error = "FS_NODE_NOT_FOUND",
                    message = ex.message
                )
            )

    @ExceptionHandler(InvalidFsOperationException::class)
    fun handleInvalidFsOperation(ex: InvalidFsOperationException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "INVALID_FS_OPERATION",
                    message = ex.message
                )
            )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = "VALIDATION_FAILED",
                    message = msg.ifBlank { ex.message }
                )
            )
    }

    // Optional generic catch-all, if you want
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    error = "INTERNAL_ERROR",
                    message = ex.message
                )
            )
}
