package com.example.backend.model.exception;

public class EvalError extends RuntimeException {
    public EvalError(String message) {
        super(message);
    }
}
