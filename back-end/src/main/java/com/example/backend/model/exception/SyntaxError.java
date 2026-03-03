package com.example.backend.model.exception;

public class SyntaxError extends RuntimeException {
    public SyntaxError(String message) {
        super(message);
    }
}
