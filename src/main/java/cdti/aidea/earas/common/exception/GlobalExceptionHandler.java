package cdti.aidea.earas.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("Validation error");

    Map<String, String> response = new HashMap<>();
    response.put("message", errorMessage);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handle invalid argument exceptions
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handle runtime exceptions
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
    // Log exception for debugging
    // log.error("An unexpected error occurred: ", ex);

    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("response", ex.getMessage());
    errorDetails.put("error", "An unexpected error occurred");
    errorDetails.put(
        "message",
        "We apologize for the inconvenience. Please try again later or contact support if the issue persists.");

    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Handle invalid JSON parse for Integer/UUID errors
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleInvalidJsonFormat(
      HttpMessageNotReadableException ex) {
    Map<String, String> response = new HashMap<>();

    // Check if the error is related to UUID or Integer parsing issue
    if (ex.getMessage().contains("UUID") || ex.getMessage().contains("Invalid UUID format")) {
      response.put("message", "Invalid UUID format. Please provide a valid UUID.");
    } else if (ex.getMessage().contains("Cannot deserialize value of type 'java.lang.Integer'")) {
      response.put("message", "Invalid Integer format. Please provide a valid number.");
    } else {
      response.put("message", "Invalid input format. Please check your request data.");
    }

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handle general response status exception (e.g., wrong endpoint or other unexpected cases)
  @ExceptionHandler(ResponseStatusException.class)
  @ResponseBody
  public ResponseEntity<Map<String, String>> handleResponseStatusException(
      ResponseStatusException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getMessage());
    return new ResponseEntity<>(response, ex.getStatusCode());
  }
}
