package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.controller.OpenFoodFactsClient;
import com.self.ZeroWasteFood.model.ProductResponse;
import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.util.FoodStickerPack;
import com.self.ZeroWasteFood.util.InMemoryUserStorage;
import com.self.ZeroWasteFood.util.Instructions;
import com.vdurmont.emoji.EmojiParser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.self.ZeroWasteFood.util.ScanStatus.COMPLETE;

@Slf4j
@Service
public class TextMessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final InMemoryUserStorage userStorage;
    private final ProductScanService productScanService;
    private final ProductService productService;
    private final OpenFoodFactsClient openFoodFactsClient;

    public TextMessageHandler(UserService userService, MessageService messageService, InMemoryUserStorage userStorage, ProductScanService productScanService, ProductService productService, OpenFoodFactsClient openFoodFactsClient) {
        this.userService = userService;
        this.messageService = messageService;
        this.userStorage = userStorage;
        this.productScanService = productScanService;
        this.productService = productService;
        this.openFoodFactsClient = openFoodFactsClient;
    }

    public void handleTextMessage(String messageText, long chatId, Update update) {
        switch (messageText) {
            case "/start":
                handleStartCommand(chatId, update);
                break;
            case "/full_scan":
                handleFullScanProductCommand(chatId, update);
                break;
            default:
                handleUnknownCommand(chatId, messageText);
                break;
        }

    }


    private void handleStartCommand(long chatId, Update update) {
        Message message = update.getMessage();

        if (message != null && message.getChat() != null) {
            String[] callBackData = {"add_me_msg"};
            String[] inlineKeyboardButton = {EmojiParser.parseToUnicode(":sparkles: Add Me")};

            if (!hasUserInDb(message.getChat().getId())) {
                messageService.sendTextMessageWithCallbackQuery(
                        chatId,
                        Instructions.registerNewUserInstruction(message.getChat().getFirstName()),
                        callBackData,
                        inlineKeyboardButton
                );

                userStorage.saveUser(chatId, message.getFrom());
                log.info("New user saved: chatId={}, username={}", chatId, message.getFrom().getUserName());
            } else {
                messageService.sendTextMessage(chatId, Instructions.infoInstructions(message.getChat().getFirstName()));
            }

            log.info("Handled /start command for chatId: {}", chatId);
        } else {
            log.error("Failed to handle /start command. Invalid message or chat data.");
        }
    }


    private boolean hasUserInDb(@NonNull Long id) {
        return userService.findUserById(id).isPresent();
    }


    private void handleFullScanProductCommand(long chatId, Update update) {
        productScanService.createProductScanWithUser(
                userService
                        .findUserById
                                (update.getMessage()
                                        .getFrom()
                                        .getId()
                                )
                        .orElseThrow()
        );
        messageService.sendTextMessageWithForceReply(
                chatId,
                "Upload photo"
        );

        log.info("Handled /full_scan command for chatId: {}", chatId);
    }

    private void handleUnknownCommand(long chatId, String messageText) {
        messageService.sendTextMessage(chatId, "Unknown command: " + messageText);
        log.info("Received unknown command: {} for chatId: {}", messageText, chatId);
    }

    public void handleReplyMessage(Message replyMessage, long chatId, Update update) {

        String text = replyMessage.getText();
        switch (text) {
            case "Enter your data:":
                handleManualInput(chatId, update);
                break;
            default:
                handleUnknownQuote(chatId, update);
                break;
        }

    }

    private void handleUnknownQuote(long chatId, Update update) {
        messageService.sendTextMessage(chatId, "Sorry, unknown quote!. Try again later");
    }

    private void handleManualInput(long chatId, Update update) {
        Long id = update.getMessage().getFrom().getId();
        ProductScan productScan = productScanService.getProductByUserId(userService.findUserById(id).orElseThrow());
        String messageFromUser = update.getMessage().getText();
        switch (productScan.getStatus()) {
            case WAITING_FOR_EXPIRATION_DATE -> {
                try {
                    productScan.setExpirationDate(new SimpleDateFormat("dd.MM.yy").parse(messageFromUser));
                } catch (ParseException e) {
                    log.error("Unable to parse expiration date '{}' : {}", messageFromUser, e.getMessage(), e);
                    messageService.sendTextMessageWithForceReply(chatId, "Enter your data: ");
                }
            }
            case WAITING_FOR_BARCODE -> {
                productScan.setBarcode(messageFromUser);
            }
        }
        productScan.setStatus(COMPLETE);
        productScanService.save(productScan);
        ProductResponse productResponse = openFoodFactsClient.fetchProductByCode(productScan.getBarcode());
        productService.addProductToUserById(id, productScan, productResponse);
        productScanService.removeCompleteProductScan(productScan);
        notifyUserAboutSuccess(chatId); //TODO use observer pattern
    }

    private void notifyUserAboutSuccess(long chatId){
        messageService.sendSticker(chatId, FoodStickerPack.RASPBERRY_ANGEL.getFileId());
    }
}
