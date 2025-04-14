package com.example.airreservation.exceptionHandler;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorType errorType;
    private final HttpStatus status;
    private final String reason;

    public BusinessException(ErrorType errorType, String reason) {
        super(reason);
        this.errorType = errorType;
        this.status = errorType.getStatus();
        this.reason = reason;
    }
}