package com.self.ZeroWasteFood.services;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class TelegramService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final UserService userService;
    private final TelegramClient telegramClient;
    private final BarCodeService barCodeService;
    private final String botToken = "7496319396:AAGx2AE3USjrLNUJXDRB06EtZD8saLqspX0";


    @Autowired
    public TelegramService(UserService userService, BarCodeService barCodeService) {
        this.userService = userService;
        this.barCodeService = barCodeService;
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
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
        long chat_id = 0;
        if (update.hasCallbackQuery()) {

            chat_id = update.getCallbackQuery().getMessage().getChatId();
            log.info("Receive call back query {}", update.getCallbackQuery().getData());
            try {
                handleCallBackQuery(chat_id, update.getCallbackQuery().getData(), update);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }else if (update.hasMessage()) {
            chat_id = update.getMessage().getChatId();
            log.info("We speak with {} at {}", update.getMessage().getChat().getFirstName(), LocalTime.now());

            if (update.getMessage().hasText()) {
                log.info("Receive message : {} ", update.getMessage());
                handleTextMessage(update.getMessage().getText(), chat_id, update);
            } else if (update.getMessage().hasPhoto()) {
                try {
                    handlePhotoMessage(update.getMessage().getPhoto(), chat_id,update);
                } catch (IOException | TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleCallBackQuery(long chatId, String callBackQuery, Update update) throws TelegramApiException {
        switch (callBackQuery) {
            case "upload_photo_msg":
                log.info("We are in the case {}", callBackQuery);
                sendTextMessageWithForceReply(chatId,"Upload your photo");
                log.info("sendTextMessageWithForceReply() was successfully executed");
                break;
            case "barcode_msg":
                log.info("We receive barcode");
                handlePhotoMessageWithBarcode(chatId,update);
                break;
        }
    }

    private void handlePhotoMessageWithBarcode(long chatId, Update update) throws TelegramApiException {
        if(update.getMessage().hasPhoto()){
            GetFile file = new GetFile(getFileId(update.getMessage().getPhoto()));
            String filePath = getFilePath(file);
            File result = getDownloadFileResult(filePath);

            try {
                BufferedImage bufferedImage = ImageIO.read(result);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
                Result decode = new MultiFormatReader().decode(binaryBitmap);
                sendTextMessage(chatId, decode.getText());
            } catch (IOException | NotFoundException e) {
                throw new RuntimeException(e);
            }

        }else {
            sendTextMessage(chatId,"We don't have any message");
        }
    }

    private void sendTextMessageWithForceReply(long chatId, String replyMessage) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(replyMessage)
                .replyMarkup(ForceReplyKeyboard.builder().selective(true).build())
                .build();

        executeMessage(message);
    }


    private void handleTextMessage(String messageText, long chatId, Update update) {
        switch (messageText) {
            case "/start":
                sendTextMessage(chatId, "Welcome! " + update.getMessage().getChat().getFirstName());
                log.info("We answered on command /start");
                break;
            case "/new":
                addNewProduct(chatId, update);
                break;
            default:
                sendTextMessage(chatId, "Unknown command: " + messageText);
                log.info("Unknown command: {}", messageText);
                break;
        }

    }

    private void addNewProduct(long chatId, Update update) {
        sendTextMessageWithCallBackQuery(chatId,
                getProductUploadInstructions(update.getMessage().getChat().getFirstName()),
                "upload_photo_msg",
                String.format("%s Upload photo", EmojiParser.parseToUnicode(":camera:"))
                );

    }

    private void sendTextMessageWithCallBackQuery(long chatId,
                                                  String text,
                                                  String callbackData,
                                                  String inlineKeyboardText
    ) {

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(
                                new InlineKeyboardRow(InlineKeyboardButton.builder()
                                        .text(inlineKeyboardText)
                                        .callbackData(callbackData)
                                        .build())
                        )
                        .build())
                .build();
        executeMessage(message);
    }

    private String getBarcodeUploadInstructions(String firstName){
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                        %s ,please reply to this message and upload a clear photo of the barcode. Ensure the barcode is well-lit and in focus for the best results.
                        """ ,
                        firstName
                )
        );
    }

    private String getProductUploadInstructions(String firstName) {
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                                Hi, %s ! %s
                                
                                %s Please upload a photo of your product for us to scan the expiration date.
                                
                                %s Focus on the Date: The expiration date should be visible and centered in the photo.
                                """,
                        firstName,
                        EmojiParser.parseToUnicode(":wave:"),
                        EmojiParser.parseToUnicode(":calendar:"),
                        EmojiParser.parseToUnicode(":bulb:")
                )
        );
    }

    private String getFileId(List<PhotoSize> photos){
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
    private void handlePhotoMessage(List<PhotoSize> photos, long chatId, Update update) throws IOException, TelegramApiException {
        String fileId = getFileId(photos);


        GetFile getFile = new GetFile(fileId);
        String filePath = getFilePath(getFile);
        File img = getDownloadFileResult(filePath);
        sendTextMessage(chatId,"Give me few seconds....");
        buildPostRequest(img, chatId, fileId,update);

    }

    private void buildPostRequest(File img, long chatId, String fileId, Update update) throws IOException {
        HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/process-image");

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addBinaryBody("image",
                        img,
                        ContentType.APPLICATION_OCTET_STREAM,
                        fileId
                );
        HttpEntity entity = entityBuilder.build();

        postRequest.setEntity(entity);

        makeRequest(postRequest, chatId,update);

    }

    private void makeRequest(HttpPost postRequest, long chatId, Update update) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(postRequest)) {

            log.info("Status code : {}",response.getStatusLine().getStatusCode());

            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(responseEntity);
                log.info("Response body : {}", responseBody);
                userService.addProductToUser(chatId, responseBody,update);
                sendTextMessage(chatId,"Done");
                sendTextMessageWithCallBackQuery(chatId,
                        getBarcodeUploadInstructions(update.getMessage().getChat().getFirstName()),
                        "barcode_msg",
                        EmojiParser.parseToUnicode(":bar_chart: Click on me and upload barcode")
                        );
                sendTextMessageWithForceReply(chatId,"Upload photo with barcode");
            }


            EntityUtils.consume(responseEntity);
        }
    }

    private void sendTextMessage(long chatId, String text) {
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
            log.info("We got a problem with message. Message info {} and {}", message.getText(), message.getChatId());
        }
    }

    private void executeMessage(SendPhoto message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.info("We got a problem with Photo message. Message info {} and {}", message.getCaption(), message.getChatId());
        }
    }

    private void sendPhotoMessage(long chatId, String filePath) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(new File(filePath)))
                .build();
        executeMessage(message);
    }

    private void sendPhotoMessage(long chatId, String fileId, String caption) {
        SendPhoto message = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(fileId))
                .caption(caption)
                .build();
        executeMessage(message);
    }
}
