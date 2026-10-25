package com.shop.presentation.advice;

import com.shop.domain.exception.*;
import com.shop.infrastructure.payment.InvalidWebhookSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(Instant timestamp, int status, String message, Map<String, String> fieldErrors) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(DomainException e) { return build(HttpStatus.NOT_FOUND, e.getMessage()); }

    @ExceptionHandler({EmailAlreadyUsedException.class, InsufficientStockException.class, InvalidOrderStateException.class})
    ResponseEntity<ErrorResponse> conflict(DomainException e) { return build(HttpStatus.CONFLICT, e.getMessage()); }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> unauthorized(DomainException e) { return build(HttpStatus.UNAUTHORIZED, e.getMessage()); }

    @ExceptionHandler(AccessDeniedDomainException.class)
    ResponseEntity<ErrorResponse> forbidden(DomainException e) { return build(HttpStatus.FORBIDDEN, e.getMessage()); }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> domain(DomainException e) { return build(HttpStatus.BAD_REQUEST, e.getMessage()); }

    @ExceptionHandler(InvalidWebhookSignatureException.class)
    ResponseEntity<ErrorResponse> webhook(InvalidWebhookSignatureException e) { return build(HttpStatus.BAD_REQUEST, e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), 400, "Validation failed", errors));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception e) { return build(HttpStatus.BAD_REQUEST, "Malformed request"); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse spring) {
            // framework exceptions that already carry their HTTP status (404, 405, 415, missing parameter...)
            HttpStatus status = HttpStatus.valueOf(spring.getStatusCode().value());
            return build(status, status.getReasonPhrase());
        }
        log.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), message, Map.of()));
    }
}
