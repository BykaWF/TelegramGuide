package com.self.ZeroWasteFood.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class CallbackQueryHandler {
    private final PhotoMessageHandler photoMessageHandler;
    private final MessageService messageService;

    public CallbackQueryHandler(PhotoMessageHandler photoMessageHandler,MessageService messageService) {
        this.photoMessageHandler = photoMessageHandler;
        this.messageService = messageService;
    }

    public void handleCallbackQuery(String callbackData, long chatId, Update update) {
        switch (callbackData) {
            case "upload_photo_msg":
                handleUploadPhoto(chatId, update);
                break;
            case "barcode_msg":
                handleBarcode(chatId, update);
                break;
            default:
                handleUnknownCallback(chatId, callbackData);
                break;
        }
    }

    private void handleUploadPhoto(long chatId, Update update) {
        log.info("Callback received: Upload Photo");
        messageService.sendTextMessageWithForceReply(chatId, "Please upload a photo of the expiration date.");
        photoMessageHandler.setWaitingForExpDate(true);
    }

    private void handleBarcode(long chatId, Update update) {
        log.info("Callback received: Barcode");
        messageService.sendTextMessageWithForceReply(chatId, "Please upload a photo of the barcode.");
        photoMessageHandler.setWaitingForBarCode(true);
    }

    private void handleUnknownCallback(long chatId, String callbackData) {
        log.warn("Unknown callback received: {}", callbackData);
        messageService.sendTextMessage(chatId, "Unknown action. Please try again.");
    }
}

