package com.self.ZeroWasteFood.util;

public enum ProcessImageRequestType {
    FULL_SCAN("full_scan"),
    BARCODE("barcode"),
    EXPIRATION_DATE("expiration_date");

    private String type;

    ProcessImageRequestType(String type) {
        this.type = type;
    }
}
