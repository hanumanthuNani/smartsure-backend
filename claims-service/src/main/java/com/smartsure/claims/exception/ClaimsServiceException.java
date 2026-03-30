package com.smartsure.claims.exception;

public class ClaimsServiceException extends RuntimeException {
    public ClaimsServiceException(String message) {
        super(message);
    }

    public ClaimsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
