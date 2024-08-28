package com.example.test;

import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class ZeroFoodWasteBot implements LongPollingSingleThreadUpdateConsumer {

    private final Logger logger = Logger.getLogger(ZeroFoodWasteBot.class.getName());
    private final TelegramClient telegramClient;

    public ZeroFoodWasteBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            long chat_id = update.getMessage().getChatId();
            logger.info("We speak with " + update.getMessage().getChat().getFirstName() + " at " + LocalTime.now());
            if (update.getMessage().hasText()) {
                handleTextMessage(update.getMessage().getText(), chat_id);
            } else if (update.getMessage().hasPhoto()) {
                try {
                    handlePhotoMessage(update.getMessage().getPhoto(), chat_id);
                } catch (IOException | TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleTextMessage(String messageText, long chatId) {
        switch (messageText) {
            case "/start":
                sendTextMessage(chatId, "Welcome!");
                logger.info("We answered on command /start");
                break;
            case "/pic":
                sendPhotoMessage(chatId, "src/main/resources/photos/close_face_flixbus.jpg");
                logger.info("We sent a picture on /pic");
                break;
            case "/markup":
                sendMarkup(chatId);
                logger.info("We show keyboard");
                break;
            case "/hide":
                hideMarkup(chatId, "Keyboard hidden");
                logger.info("Keyboard hidden");
                break;
            case "Greeting":
                sendTextMessage(chatId, EmojiParser.parseToUnicode("Hello, how are you ? :smile:"));
                break;
            case "Picture":
                sendPhotoMessage(chatId, "src/main/resources/photos/close_face_flixbus.jpg");
                break;
            case "New Food":
                sendTextMessage(chatId, "Upload your photo ...");
                break;
            default:
                sendTextMessage(chatId, "Unknown command: " + messageText);
                logger.info("Unknown command: " + messageText);
                break;
        }

        // Echo the received message
        sendTextMessage(chatId, messageText);
    }

    private void hideMarkup(long chatId, String text) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(Boolean.TRUE))
                .build();
        executeMessage(sendMessage);
    }

    private void sendMarkup(long chatId) {
        SendMessage message = SendMessage
                .builder()
                .text("Here is your keyboard")
                .chatId(chatId)
                .build();
        message.setReplyMarkup(ReplyKeyboardMarkup
                .builder()
                .keyboardRow(new KeyboardRow("Greeting", "Picture", "New Food"))
                .build()
        );

        executeMessage(message);
    }

    private void handlePhotoMessage(List<PhotoSize> photos, long chatId) throws IOException, TelegramApiException {
        String fileId = photos.stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("");



        GetFile getFile = new GetFile(fileId);
        String filePath = telegramClient.execute(getFile).getFilePath();
        File img = telegramClient.downloadFile(filePath);
        buildPostRequest(img,chatId,fileId);

    }

    private void buildPostRequest(File img, long chatId, String fileId) throws IOException {
        HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/process-image");

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addBinaryBody("image",
                        img,
                        ContentType.APPLICATION_OCTET_STREAM,
                        fileId
                );
        HttpEntity entity = entityBuilder.build();

        postRequest.setEntity(entity);

        makeRequest(postRequest,chatId);

    }

    private void makeRequest(HttpPost postRequest, long chatId) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(postRequest)) {
            logger.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity != null){
                String responseBody = EntityUtils.toString(responseEntity);
                sendTextMessage(chatId,responseBody);
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
            logger.info("We got a problem with message. Message info " + message.getText() + " and " + message.getChatId());
        }
    }

    private void executeMessage(SendPhoto message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.info("We got a problem with message. Message info " + message.getCaption() + " and " + message.getChatId());
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
