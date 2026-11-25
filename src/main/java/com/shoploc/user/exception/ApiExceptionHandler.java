package com.shoploc.user.exception;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns our exceptions into HTTP errors (format application/problem+json). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problem.setProperty("code", "INVALID_CREDENTIALS");
        return problem;
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ProblemDetail handleAccountNotActive(AccountNotActiveException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "This account cannot log in");
        problem.setProperty("code", e.getCode());
        if (e.getMerchantRequestStatus() != null) {
            problem.setProperty("merchantRequestStatus", e.getMerchantRequestStatus());
        }
        if (e.getRefusalReason() != null) {
            problem.setProperty("refusalReason", e.getRefusalReason());
        }
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setProperty("code", "EMAIL_ALREADY_EXISTS");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        List<Object> errors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> (Object) Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage()
            ))
            .toList();
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);
        return problem;
    }
}
