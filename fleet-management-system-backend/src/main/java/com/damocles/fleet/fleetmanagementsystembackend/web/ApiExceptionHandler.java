package com.damocles.fleet.fleetmanagementsystembackend.web;

import com.damocles.fleet.fleetmanagementsystembackend.exception.*;
import com.damocles.fleet.fleetmanagementsystembackend.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Exception -> HTTP status mapping:
    // - NotFoundException (+ *NotFoundException): 404
    // - ConflictException / BusinessValidationException / TransportNotActiveException: 409
    // - InvalidCredentialsException / AuthenticationException: 401
    // - ForbiddenException / AccessDeniedException / DisabledException: 403
    // - MethodArgumentNotValidException / IllegalArgumentException: 400
    // - Fallback Exception: 500

    // =========================
    // Helpers
    // =========================
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    // =========================
    // 404
    // =========================
    @ExceptionHandler({
            NotFoundException.class,
            UserNotFoundException.class,
            DriverNotFoundException.class,
            VehicleNotFoundException.class,
            TrailerNotFoundException.class,
            LocationNotFoundException.class,
            TransportNotFoundException.class,
            CargoNotFoundException.class,
            DriverWorkLogNotFoundException.class,
            TransportStatusNotFoundException.class
    })
    public ResponseEntity<ApiError> notFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // 409
    @ExceptionHandler({
            ConflictException.class,
            EmailAlreadyUsedException.class,
            BusinessValidationException.class,
            TransportNotActiveException.class
    })
    public ResponseEntity<ApiError> conflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // =========================
    // 403
    // =========================
    @ExceptionHandler({
            AccessDeniedException.class,
            DisabledException.class,
    })
    public ResponseEntity<ApiError> forbidden(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", req);
    }

    // =========================
    // 401 (login)
    // =========================
    @ExceptionHandler({
            AuthenticationException.class,
            InvalidCredentialsException.class
    })
    public ResponseEntity<ApiError> unauthorized(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }


    // =========================
    // 400 (validation @Valid)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );
        String msg = fieldErrors.isEmpty() ? "Validation error" : "Validation failed";
        return ResponseEntity.badRequest().body(ApiError.validation(msg, req.getRequestURI(), fieldErrors));
    }

    // =========================
    // 400 (arguments / enum / parse)
    // =========================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // =========================
    // 500 fallback
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> other(Exception ex, HttpServletRequest req) {
        // log exception in service/logger
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    // ForbiddenException (business rule)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> forbiddenBiz(ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

}
