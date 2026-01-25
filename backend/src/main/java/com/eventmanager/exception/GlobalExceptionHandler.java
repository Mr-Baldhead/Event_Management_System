package com.eventmanager.exception;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;

// Global exception handler for JAX-RS REST endpoints
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        // Handle ResourceNotFoundException
        if (exception instanceof ResourceNotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }

        // Handle RegistrationFullException
        if (exception instanceof RegistrationFullException) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage());
        }

        // Handle IllegalArgumentException
        if (exception instanceof IllegalArgumentException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        // Handle ConstraintViolationException (validation errors)
        if (exception instanceof ConstraintViolationException cve) {
            StringBuilder message = new StringBuilder("Validation failed: ");
            cve.getConstraintViolations().forEach(violation ->
                message.append(violation.getPropertyPath())
                    .append(" ")
                    .append(violation.getMessage())
                    .append("; ")
            );
            return buildResponse(Response.Status.BAD_REQUEST, message.toString());
        }

        // Handle all other exceptions
        return buildResponse(
            Response.Status.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + exception.getMessage()
        );
    }

    private Response buildResponse(Response.Status status, String message) {
        JsonObject error = Json.createObjectBuilder()
            .add("timestamp", LocalDateTime.now().toString())
            .add("status", status.getStatusCode())
            .add("error", status.getReasonPhrase())
            .add("message", message)
            .build();

        return Response.status(status)
            .entity(error.toString())
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
