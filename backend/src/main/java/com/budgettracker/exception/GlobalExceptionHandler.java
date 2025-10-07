package com.budgettracker.exception;

import com.budgettracker.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex,
            WebRequest request) {
        log.error("Business exception: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {
        log.error("Unauthorized access: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("UNAUTHORIZED")
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex,
            WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message(ex.getMessage())
                .details(ex.getErrors())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle MethodArgumentNotValidException (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("Constraint violation")
                .details(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .message("You don't have permission to access this resource")
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle BadCredentialsException
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("BAD_CREDENTIALS")
                .message("Invalid username or password")
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("ILLEGAL_ARGUMENT")
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error));
    }
}
