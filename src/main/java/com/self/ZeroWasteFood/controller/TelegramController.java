package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.services.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.time.LocalTime;

@Slf4j
@Component
public class TelegramController implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Getter
    private final TelegramClient telegramClient;
    private final TextMessageHandler textMessageHandler;
    private final PhotoMessageHandler photoMessageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final String botToken;
    private final UserService userService;
    private final MessageService messageService;

    @Autowired
    public TelegramController(
            TelegramClient telegramClient, TextMessageHandler textMessageHandler,
            PhotoMessageHandler photoMessageHandler,
            CallbackQueryHandler callbackQueryHandler,
            @Value("${telegram.bot.token}") String botToken, UserService userService, MessageService messageService
    ) {
        this.telegramClient = telegramClient;
        this.textMessageHandler = textMessageHandler;
        this.photoMessageHandler = photoMessageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.botToken = botToken;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        long chatId = update.getMessage() != null ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();

        try {
            if (update.hasCallbackQuery()) {
                log.info("Receive callback query {}", update.getCallbackQuery().getData());
                callbackQueryHandler.handleCallbackQuery(update.getCallbackQuery().getData(), chatId, update);
            } else if (update.hasMessage()) {
                log.info("We speak with {} at {}", update.getMessage().getChat().getFirstName(), LocalTime.now());
                if (update.getMessage().hasText()) {
                    log.info("Receive message: {}", update.getMessage().getText());
                    if(update.getMessage().getReplyToMessage() != null){
                        textMessageHandler.handleReplyMessage(update.getMessage().getReplyToMessage(), chatId, update);
                    }else{

                        textMessageHandler.handleTextMessage(update.getMessage().getText(), chatId, update);
                    }

                } else if (update.getMessage().hasPhoto()) {
                    photoMessageHandler.handlePhotoMessage(update.getMessage().getPhoto(), chatId, update);
                }
            }
        } catch (IOException | TelegramApiException e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }
}