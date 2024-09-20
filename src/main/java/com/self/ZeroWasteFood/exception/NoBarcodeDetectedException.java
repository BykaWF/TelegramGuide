package com.self.ZeroWasteFood.exception;

public class NoBarcodeDetectedException extends RuntimeException {
    public NoBarcodeDetectedException(String message) {
        super(message);
    }
}
