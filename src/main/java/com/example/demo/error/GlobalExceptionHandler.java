package com.example.demo.error;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.auth.EmailAlreadyInUseException;
import com.example.demo.auth.InvalidRefreshTokenException;
import com.example.demo.auth.UsernameAlreadyInUseException;
import com.example.demo.name.NameNotFoundException;
import com.example.demo.security.UnauthenticatedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ProblemDetail> handleEmailInUse(EmailAlreadyInUseException ex) {
        return problem(HttpStatus.CONFLICT, "email-already-in-use", ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    public ResponseEntity<ProblemDetail> handleUsernameInUse(UsernameAlreadyInUseException ex) {
        return problem(HttpStatus.CONFLICT, "username-already-in-use", ex.getMessage());
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNameNotFound(NameNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "name-not-found", ex.getMessage());
    }

    @ExceptionHandler({InvalidRefreshTokenException.class, UnauthenticatedException.class})
    public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "unauthorized", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Invalid username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "You do not have permission to perform this action");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setTitle("Validation error");
        detail.setType(URI.create("urn:problem-type:validation-error"));
        detail.setProperty("fieldErrors", ex.getBindingResult().getFieldErrors().stream()
                .map(field -> new FieldError(field.getField(), String.valueOf(field.getRejectedValue()), field.getDefaultMessage()))
                .toList());
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "An unexpected error occurred");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("urn:problem-type:" + code));
        return ResponseEntity.status(status).body(problemDetail);
    }

    public record FieldError(String field, Object rejectedValue, String message) {
    }
}