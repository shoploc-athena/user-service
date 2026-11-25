package com.shoploc.user.exception;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns our exceptions into HTTP errors (format application/problem+json). */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String CODE = "code";
    private static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, e.getMessage(), "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ProblemDetail handleAccountNotActive(AccountNotActiveException e) {
        ProblemDetail problem = problem(HttpStatus.FORBIDDEN, "This account cannot log in", e.getCode());
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
        return problem(HttpStatus.CONFLICT, e.getMessage(), EMAIL_ALREADY_EXISTS);
    }

    @ExceptionHandler(MerchantRequestNotFoundException.class)
    public ProblemDetail handleMerchantRequestNotFound(MerchantRequestNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), "MERCHANT_REQUEST_NOT_FOUND");
    }

    @ExceptionHandler(MerchantRequestAlreadyDecidedException.class)
    public ProblemDetail handleMerchantRequestAlreadyDecided(MerchantRequestAlreadyDecidedException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage(), "MERCHANT_REQUEST_ALREADY_DECIDED");
    }

    /** Two decisions at the same time: the database refuses the second one (optimistic lock). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrentUpdate(OptimisticLockingFailureException e) {
        return problem(HttpStatus.CONFLICT, "The request was changed by someone else, please retry", "CONCURRENT_UPDATE");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException e) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR");
        List<Object> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> (Object) Map.of(
                        "field", error.getField(),
                        "message", String.valueOf(error.getDefaultMessage())))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "Email already exists", EMAIL_ALREADY_EXISTS);
    }

    private static ProblemDetail problem(HttpStatus status, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE, code);
        return problem;
    }
}