package com.example.Exception;

import lombok.Getter;

@Getter
public class AlphaVantageException extends RuntimeException{

    private final ErrorType errorType;

    public enum ErrorType {
        API_LIMIT_EXCEEDED,
        INVALID_API_KEY,
        SERVICE_UNAVAILABLE,
        INVALID_RESPONSE,
        NO_DATA_FOUND,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

    public AlphaVantageException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public AlphaVantageException(String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

}
