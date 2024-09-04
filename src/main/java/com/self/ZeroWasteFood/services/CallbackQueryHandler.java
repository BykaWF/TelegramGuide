package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.util.InMemoryUserStorage;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
public class CallbackQueryHandler {
    private final PhotoMessageHandler photoMessageHandler;
    private final MessageService messageService;
    private final UserService userService;
    private final InMemoryUserStorage inMemoryUserStorage;
    public CallbackQueryHandler(PhotoMessageHandler photoMessageHandler, MessageService messageService, UserService userService, InMemoryUserStorage inMemoryUserStorage) {
        this.photoMessageHandler = photoMessageHandler;
        this.messageService = messageService;
        this.userService = userService;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void handleCallbackQuery(String callbackData, long chatId, Update update) {
        switch (callbackData) {
            case "upload_photo_msg":
                handleUploadPhoto(chatId, update);
                break;
            case "barcode_msg":
                handleBarcode(chatId, update);
                break;
            case "add_me_msg":
                handleAddUser(chatId,update);
                break;
            default:
                handleUnknownCallback(chatId, callbackData);
                break;
        }
    }

    private void handleAddUser(long chatId, Update update) {
        messageService.sendTextMessage(chatId, EmojiParser.parseToUnicode(":sparkles:"));

        User currentUser =  inMemoryUserStorage.getUserByKey(chatId);
        userService.addUserAsTelegramUser(currentUser);
        log.info("We get user from update {}", currentUser);
        log.info("We saved him into database");

        messageService.sendTextMessage(chatId,EmojiParser.parseToUnicode(":thumbsup: We added you!"));
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

