package com.self.ZeroWasteFood.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;

@Slf4j
@Service
public class MessageService {

    private final TelegramClient telegramClient;

    @Autowired
    public MessageService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendTextMessageWithCallbackQuery(long chatId, String text, String callbackData, String inlineKeyboardText) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(buildInlineKeyboardMarkup(inlineKeyboardText, callbackData))
                .build();
        executeMessage(message);
    }

    public void sendTextMessageWithForceReply(long chatId, String replyMessage) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(replyMessage)
                .replyMarkup(ForceReplyKeyboard.builder().selective(true).build())
                .build();
        executeMessage(message);
    }

    public void sendPhotoMessage(long chatId, String fileId, String caption) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(fileId))
                .caption(caption)
                .build();
        executeMessage(message);
    }

    public void sendTextMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {} to chat {}", message.getText(), message.getChatId(), e);
        }
    }

    private void executeMessage(SendPhoto message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send photo message to chat {}", message.getChatId(), e);
        }
    }

    private InlineKeyboardMarkup buildInlineKeyboardMarkup(String buttonText, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
    }
}
