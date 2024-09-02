package com.self.ZeroWasteFood.services;

import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.self.ZeroWasteFood.controller.OpenFoodFactsController;
import com.self.ZeroWasteFood.model.Product;
import com.self.ZeroWasteFood.model.ProductResponse;
import com.self.ZeroWasteFood.util.BarCodeUtils;
import com.self.ZeroWasteFood.util.Instructions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class PhotoMessageHandler {

    private boolean isWaitingForBarCode = false;
    private boolean isWaitingForExpDate = false;
    private final UserService userService;
    private final ExpirationDateExtractionService extractionService;
    private final MessageService messageService;
    private final TelegramClient telegramClient;
    private final OpenFoodFactsController openFoodFactsController;
    @Autowired
    public PhotoMessageHandler(UserService userService, ExpirationDateExtractionService extractionService, MessageService messageService, TelegramClient telegramClient, OpenFoodFactsController openFoodFactsController) {
        this.userService = userService;
        this.extractionService = extractionService;
        this.messageService = messageService;
        this.telegramClient = telegramClient;
        this.openFoodFactsController = openFoodFactsController;
    }

    public void handlePhotoMessage(List<PhotoSize> photos, long chatId, Update update) throws IOException, TelegramApiException {
        String fileId = getFileId(photos);

        File img = getFile(fileId);

        messageService.sendTextMessage(chatId, "Give me few seconds....");

        if (isWaitingForExpDate) {
            String responseBody = extractionService.buildPostRequest(img, chatId, fileId, update);
            userService.addProductToUser(chatId, responseBody, update);
            messageService.sendTextMessage(chatId, "Done");
            isWaitingForExpDate = false;
            isWaitingForBarCode = true;
            messageService.sendTextMessageWithForceReply(chatId, Instructions.barcodeUploadInstructions(update.getMessage().getChat().getFirstName()));
        } else if (isWaitingForBarCode) {
            try {
                Result decode = BarCodeUtils.extractBarCodeFromImage(img);
                ProductResponse productResponse = openFoodFactsController.fetchProductByCode(decode.getText());
                Product product = productResponse.getProduct();
                String productName = product.getProductName();

                String imageFrontSmallUrl = product.getImageFrontSmallUrl();
                messageService.sendTextMessage(chatId,imageFrontSmallUrl);
                messageService.sendTextMessage(chatId,productName);
                isWaitingForBarCode = false;
            } catch (IOException e) {
                log.error("Can't read image");
                messageService.sendTextMessage(chatId, "Try upload photo again!");
            } catch (NotFoundException e) {
                log.error("Can't find barcode on image");
                messageService.sendTextMessage(chatId, "Can't read barcode from image. Make sure it's in the middle of image");
            }
        }


    }

    private File getFile(String fileId) throws TelegramApiException {
        GetFile getFile = new GetFile(fileId);
        String filePath = getFilePath(getFile);
        return getDownloadFileResult(filePath);
    }


    private String getFileId(List<PhotoSize> photos) {
        return photos.stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("");
    }

    private String getFilePath(GetFile file) throws TelegramApiException {
        return telegramClient.execute(file).getFilePath();
    }

    private File getDownloadFileResult(String filePath) throws TelegramApiException {
        return telegramClient.downloadFile(filePath);
    }

    public void setWaitingForBarCode(boolean waitingForBarCode) {
        isWaitingForBarCode = waitingForBarCode;
    }

    public void setWaitingForExpDate(boolean waitingForExpDate) {
        isWaitingForExpDate = waitingForExpDate;
    }
}
