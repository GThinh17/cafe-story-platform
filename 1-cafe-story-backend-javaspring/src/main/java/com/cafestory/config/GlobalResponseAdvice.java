package com.cafestory.config;

import com.cafestory.dto.responseDTO.VnpayIpnResponseDTO;
import com.cafestory.until.FormatResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.stream.Collectors;

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

        if (isOpenApiRequest(request)) {
            return body;
        }

        // If the controller already returned a FormatResponse manually, don't wrap it
        // again
        if (body instanceof FormatResponse) {
            return body;
        }
        if (body instanceof VnpayIpnResponseDTO) {
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

    private boolean isOpenApiRequest(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
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
    public ResponseEntity<FormatResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid request parameter: " + ex.getName();
        if (ex.getRequiredType() != null && ex.getRequiredType().getSimpleName().equals("UUID")) {
            message = ex.getName() + " must be a valid UUID";
        }

        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Fail",
                message,
                null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FormatResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Fail",
                message,
                null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FormatResponse<Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Fail",
                "Request body is invalid or contains invalid field format",
                null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FormatResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        FormatResponse<Object> errorResponse = new FormatResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Fail",
                ex.getMessage(),
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

    private String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid value";
        }
        return fieldError.getField() + ": " + message;
    }
}
