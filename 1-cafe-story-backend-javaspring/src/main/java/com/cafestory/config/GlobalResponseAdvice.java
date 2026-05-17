package com.cafestory.config;

import com.cafestory.until.FormatResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public GlobalResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Apply this advice to all controllers
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {

        // If the controller already returned a FormatResponse manually, don't wrap it
        // again
        if (body instanceof FormatResponse) {
            return body;
        }

        // Determine the HTTP Status code of the response
        int statusCode = 200;
        if (response instanceof ServletServerHttpResponse) {
            statusCode = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        }

        String status = (statusCode >= 200 && statusCode < 300) ? "Success" : "Fail";
        String message = status.equals("Success") ? "Request processed successfully" : "An error occurred";

        FormatResponse<Object> formatResponse = new FormatResponse<>(statusCode, status, message, body);

        // Special case: if the controller returns a String (like your /hello endpoint),
        // Spring tries to use StringHttpMessageConverter which expects a String.
        // We have to convert our FormatResponse to a JSON string manually to avoid a
        // ClassCastException.
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(formatResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error formatting JSON response", e);
            }
        }

        return formatResponse;
    }

    // This handles any uncaught exceptions thrown in your app and formats them as a
    // "Fail" response
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<FormatResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        FormatResponse<Object> errorResponse = new FormatResponse<>(
                statusCode,
                "Fail",
                ex.getReason(),
                null);

        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FormatResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = "Invalid request parameter: " + ex.getName();
        if (UUID.class.equals(ex.getRequiredType())) {
            message = ex.getName() + " must be a valid UUID";
        }

        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Fail",
                message,
                null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FormatResponse<Object>> handleAllExceptions(Exception ex) {
        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Fail",
                ex.getMessage(), // You can replace this with a generic string for production security
                null);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
