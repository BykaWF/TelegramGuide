package com.self.ZeroWasteFood.exception;

public class NoUserByIdException extends RuntimeException {
    public NoUserByIdException(String message) {
        super(message);
    }
}
