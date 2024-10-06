package com.self.ZeroWasteFood.util;

import lombok.Getter;

@Getter
public enum FoodStickerPack {
    RASPBERRY_ANGEL("CAACAgIAAxkBAAEuNxxnAsXYGFW8qtkiiQOTxDEEwZ8w6gACtgADFkJrCiejHCjy98eBNgQ");

    private String fileId;

    FoodStickerPack(String fileId){
        this.fileId = fileId;
    }
}
