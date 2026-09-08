package com.deptflow.api.web;

import com.deptflow.application.exceptions.ForbiddenException;
import com.deptflow.application.exceptions.NotFoundException;
import com.deptflow.application.exceptions.StorageException;
import com.deptflow.application.exceptions.TenantRequiredException;
import com.deptflow.application.exceptions.ValidationException;
import com.deptflow.domain.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps application and domain exceptions to RFC 7807 problem responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    ProblemDetail forbidden(ForbiddenException e) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage());
    }

    @ExceptionHandler(TenantRequiredException.class)
    ProblemDetail tenantRequired(TenantRequiredException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    ProblemDetail validation(ValidationException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Failed", e.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Operation", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", "The operation violates a data constraint");
    }

    @ExceptionHandler(StorageException.class)
    ProblemDetail storage(StorageException e) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Storage Error", e.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        return pd;
    }
}
