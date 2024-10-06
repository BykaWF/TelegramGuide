package com.self.ZeroWasteFood.services;

import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.self.ZeroWasteFood.controller.OpenFoodFactsClient;
import com.self.ZeroWasteFood.exception.NoUserByIdException;
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
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class PhotoMessageHandler {

    private boolean isWaitingForBarCode = false;
    private boolean isWaitingForExpDate = false;
    private final ProcessImageService extractionService;
    private final MessageService messageService;
    private final TelegramClient telegramClient;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final ProductService productService;

    @Autowired
    public PhotoMessageHandler(ProcessImageService extractionService, MessageService messageService, TelegramClient telegramClient, OpenFoodFactsClient openFoodFactsClient, ProductService productService) {
        this.extractionService = extractionService;
        this.messageService = messageService;
        this.telegramClient = telegramClient;
        this.openFoodFactsClient = openFoodFactsClient;
        this.productService = productService;
    }

    public void handlePhotoMessage(List<PhotoSize> photos, long chatId, Update update) throws IOException, TelegramApiException {
        String fileId = getFileId(photos);
        File img = getFile(fileId);
        extractionService.getProcessImageResponseAndAddProductToUser(
                img,
                update);
    }
    @Deprecated
    public void handlePhotoMessageOld(List<PhotoSize> photos, long chatId, Update update) throws IOException, TelegramApiException {
        String fileId = getFileId(photos);

        File img = getFile(fileId);
        messageService.sendTextMessage(chatId, "Give me few seconds....");
        extractionService.getProcessImageResponseAndAddProductToUser(
                img,
                update);
        messageService.sendTextMessage(chatId, "Done");


        if (isWaitingForBarCode) {
            try {
                Result decode = BarCodeUtils.extractBarCodeFromImage(img);
                ProductResponse productResponse = openFoodFactsClient.fetchProductByCode(decode.getText());
                log.info("Product name {}", productResponse.getProduct().getProductName());
                productService.addProductToUserById(update.getMessage().getChat().getId(), productResponse, "");
                isWaitingForBarCode = false;
                isWaitingForExpDate = true;
                messageService.sendTextMessageWithForceReply(chatId, Instructions.productUploadInstructions(update.getMessage().getChat().getFirstName()));
            } catch (IOException e) {
                log.error("Can't read image");
                messageService.sendTextMessage(chatId, "Try upload photo again!");
            } catch (NotFoundException e) {
                log.error("Can't find barcode on image");
                messageService.sendTextMessage(chatId, "Can't read barcode from image. Make sure it's in the middle of image");
            } catch (NoUserByIdException e) {
                log.error("Can't find user by id");
                messageService.sendTextMessage(chatId, "Some problem occur during request. Try again!");
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
        } else if (isWaitingForExpDate) {

            String responseBody = extractionService.getProcessImageResponseAndAddProductToUser(img, chatId, fileId, update);
            productService.addExpirationDateToExistingProductById(update.getMessage().getChat().getId(), responseBody);
            messageService.sendTextMessage(chatId, "Done");
            isWaitingForExpDate = false;
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
