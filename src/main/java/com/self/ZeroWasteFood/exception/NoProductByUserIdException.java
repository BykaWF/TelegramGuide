package com.self.ZeroWasteFood.exception;

public class NoProductByUserIdException extends RuntimeException{
    public NoProductByUserIdException(String message) {
        super(message);
    }
}