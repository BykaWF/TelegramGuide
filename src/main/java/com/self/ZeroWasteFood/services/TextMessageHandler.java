package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.util.InMemoryUserStorage;
import com.self.ZeroWasteFood.util.Instructions;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class TextMessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final InMemoryUserStorage userStorage;
    public TextMessageHandler(UserService userService, MessageService messageService, InMemoryUserStorage userStorage) {
        this.userService = userService;
        this.messageService = messageService;
        this.userStorage = userStorage;
    }

    public void handleTextMessage(String messageText, long chatId, Update update) {
        switch (messageText) {
            case "/start":
                handleStartCommand(chatId, update);
                break;
            case "/new":
                handleNewProductCommand(chatId, update);
                break;
            default:
                handleUnknownCommand(chatId, messageText);
                break;
        }
    }

    private void handleStartCommand(long chatId, Update update) {
        messageService.sendTextMessageWithCallbackQuery(
                chatId,
                Instructions.registerNewUserInstruction(update.getMessage().getChat().getFirstName()),
                "add_me_msg",
                EmojiParser.parseToUnicode(":sparkles: Add Me")
        );

        userStorage.storeUser(chatId,update.getMessage().getFrom());
        log.info("Handled /start command for chatId: {}", chatId);
    }

    private void handleNewProductCommand(long chatId, Update update) {
        messageService.sendTextMessageWithCallbackQuery(chatId,
                Instructions.barcodeUploadInstructions(update.getMessage().getChat().getFirstName()),
                "barcode_msg",
                String.format("%s Upload photo", EmojiParser.parseToUnicode(":camera:"))
        );
        log.info("Handled /new command for chatId: {}", chatId);
    }

    private void handleUnknownCommand(long chatId, String messageText) {
        messageService.sendTextMessage(chatId, "Unknown command: " + messageText);
        log.info("Received unknown command: {} for chatId: {}", messageText, chatId);
    }
}
