package com.featureflag.exception;

import com.featureflag.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                    .build();
        }
        if (exception instanceof ConflictException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("CONFLICT", e.getMessage()))
                    .build();
        }
        if (exception instanceof UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("UNAUTHORIZED", e.getMessage()))
                    .build();
        }
        if (exception instanceof ConstraintViolationException e) {
            String details = e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("VALIDATION_ERROR", "Validation failed", java.util.Map.of("violations", details)))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                .build();
    }
}
