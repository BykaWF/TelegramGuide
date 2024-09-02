package com.self.ZeroWasteFood.services;

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
    public TextMessageHandler(UserService userService,  MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
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
        String welcomeMessage = "Welcome! " + update.getMessage().getChat().getFirstName();
        messageService.sendTextMessage(chatId, welcomeMessage);
        log.info("Handled /start command for chatId: {}", chatId);
    }

    private void handleNewProductCommand(long chatId, Update update) {
        messageService.sendTextMessageWithCallbackQuery(chatId,
                Instructions.productUploadInstructions(update.getMessage().getChat().getFirstName()),
                "upload_photo_msg",
                String.format("%s Upload photo", EmojiParser.parseToUnicode(":camera:"))
        );
        log.info("Handled /new command for chatId: {}", chatId);
    }

    private void handleUnknownCommand(long chatId, String messageText) {
        messageService.sendTextMessage(chatId, "Unknown command: " + messageText);
        log.info("Received unknown command: {} for chatId: {}", messageText, chatId);
    }
}
