package com.example.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AlphaVantageException.class)
    public ResponseEntity<Map<String, Object>> handleAlphaVantageException(AlphaVantageException ex) {
        log.error("Alpha Vantage error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Alpha Vantage Error");
        response.put("message", ex.getMessage());
        response.put("errorType", ex.getErrorType().name());

        // Determina lo status HTTP appropriato in base al tipo di errore
        HttpStatus status = switch (ex.getErrorType()) {
            case API_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case INVALID_API_KEY -> HttpStatus.UNAUTHORIZED;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case NO_DATA_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(response, status);
    }

    // Gestione di altre eccezioni
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "Si è verificato un errore durante l'elaborazione della richiesta");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
