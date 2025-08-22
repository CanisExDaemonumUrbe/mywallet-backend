package com.cedu.exception;

public class InvalidAccountKindException extends RuntimeException {
    public InvalidAccountKindException(String message) {
        super(message);
    }
}
