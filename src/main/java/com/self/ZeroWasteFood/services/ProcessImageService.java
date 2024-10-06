package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.controller.OpenFoodFactsClient;
import com.self.ZeroWasteFood.controller.ProcessImageClient;
import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import com.self.ZeroWasteFood.model.ProductResponse;
import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.util.FileMultipart;
import com.self.ZeroWasteFood.util.ResponseHandler;
import com.self.ZeroWasteFood.util.ScanStatus;
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
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.self.ZeroWasteFood.util.ProcessImageRequestType.FULL_SCAN;
import static com.self.ZeroWasteFood.util.ScanStatus.*;

@Slf4j
@Service
public class ProcessImageService {
    private final ProcessImageClient processImageClient;
    private final ProductService productService;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final ProductScanService productScanService;
    private final MessageService messageService;
    private final UserService userService;

    public ProcessImageService(ProcessImageClient processImageClient, ProductService productService, OpenFoodFactsClient openFoodFactsClient, ProductScanService productScanService, MessageService messageService, UserService userService) {
        this.processImageClient = processImageClient;
        this.productService = productService;
        this.openFoodFactsClient = openFoodFactsClient;
        this.productScanService = productScanService;
        this.messageService = messageService;
        this.userService = userService;
    }


    public String getProcessImageResponseAndAddProductToUser(File img, long chatId, String fileId, Update update) throws IOException {
        HttpPost postRequest = new HttpPost("http://127.0.0.1:5005/process-image");
        log.info("We get img and building postRequest");
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                .addBinaryBody("image",
                        img,
                        ContentType.APPLICATION_OCTET_STREAM,
                        fileId
                );
        HttpEntity entity = entityBuilder.build();

        postRequest.setEntity(entity);

        return makeRequest(postRequest, chatId, update);
    }

    public String makeRequest(HttpPost postRequest, long chatId, Update update) throws IOException {
        String responseBody = "No found expiration date";
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(postRequest)) {

            log.info("Status code : {}", Optional.of(response.getStatusLine().getStatusCode()));

            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                responseBody = EntityUtils.toString(responseEntity);
                log.info("Response body : {}", responseBody);
                EntityUtils.consume(responseEntity);
                return responseBody;
            }
        }

        return responseBody;
    }

    /**
     * Use when we have barcode and expiration date on the picture.
     *
     * @param img    image from chat
     * @param update represent updates from chat
     */
    public void getProcessImageResponseAndAddProductToUser(File img, Update update) {
        // Fetch the image response
        ProcessImageResponse response = processImageClient.fetchProcessImageResponse(new FileMultipart(img.toPath()), String.valueOf(FULL_SCAN));
        ResponseHandler responseHandler = handleResponse(response);
        ProductScan productScan = getProductScan(update);
        ProductResponse productResponse = new ProductResponse();
        log.info("We have fetched response {}", response);

        if (!responseHandler.getBarcode().isBlank()) {
            String barcode = responseHandler.getBarcode();
            productScan.setBarcode(barcode);
            productResponse = openFoodFactsClient.fetchProductByCode(barcode);

        } else {
            log.error("We can't detect barcode");
            productScan.setStatus(WAITING_FOR_BARCODE);
        }

        String expirationDateStr = responseHandler.getExpirationDate();
        if (!expirationDateStr.isBlank()) {
            try {
                productScan.setExpirationDate(new SimpleDateFormat("dd.MM.yy").parse(expirationDateStr));
            } catch (ParseException e) {
                log.error("Unable to parse expiration date '{}': {}", expirationDateStr, e.getMessage(), e);
                //TODO we can't parse and aks to type manually
                messageService.sendTextMessage(update.getMessage().getChatId(), "We can't parse your date. Type manually");
            }
        } else {
            log.error("We can't detect expiration date on image");
            updateProductScanStatus(productScan);
        }

        if (productScan.getStatus() == null) {
            productScan.setStatus(COMPLETE);
        }


        productScanService.save(productScan);
        responseToUserBasedOnStatus(productScan, productResponse, update);
    }

    private void responseToUserBasedOnStatus(ProductScan productScan, ProductResponse productResponse, Update update) {
        Long chatId = update.getMessage().getChatId();
        ScanStatus status = productScan.getStatus();
        Long userId = update.getMessage().getFrom().getId();
        String[] callBackData = {"try_add_manually"};
        String[] inlineButtonText = {
                EmojiParser.parseToUnicode(":raised_hand: Manually")
        };

        switch (status) {
            case WAITING_FOR_BARCODE_AND_EXPIRATION_DATE -> {
                if (!productScanService.removeEmptyProductScan(productScan)) {
                    log.error("We can't delete empty product scan {}", productScan);
                    messageService.sendTextMessage(chatId, "Sorry, internal error occurred on server. Try later!");
                } else {
                    messageService.sendTextMessageWithForceReply(chatId, "We couldn't detect both the barcode and expiration date. You can try again ");
                }
            }
            case WAITING_FOR_BARCODE ->{
                productScanService.save(productScan);
                messageService.sendTextMessageWithCallbackQuery(
                        chatId,
                        "Barcode detection failed. Please enter the barcode manually.",
                        callBackData,
                        inlineButtonText);
            }
            case WAITING_FOR_EXPIRATION_DATE -> {
                productScanService.save(productScan);
                messageService.sendTextMessageWithCallbackQuery(
                        chatId,
                        "Expiration date detection failed. Please enter expiration date manually.",
                        callBackData,
                        inlineButtonText
                );
            }

            case COMPLETE -> {
                productService.addProductToUserById(userId, productScan, productResponse);
                if (productScanService.removeCompleteProductScan(productScan)) {
                    messageService.sendTextMessage(chatId, "Product added successfully! Barcode: " + productScan.getBarcode() + ", Expiration Date: " + productScan.getExpirationDate());
                } else {
                    messageService.sendTextMessage(chatId, "Product added successfully, but couldn't remove the scan due to an incomplete status.");
                }
            }

            default -> messageService.sendTextMessage(chatId, "An unexpected error occurred. Please try again.");
        }


   }


    private void updateProductScanStatus(ProductScan productScan) {
        if (productScan.getStatus().equals(WAITING_FOR_BARCODE)) {
            productScan.setStatus(WAITING_FOR_BARCODE_AND_EXPIRATION_DATE);
        } else {
            productScan.setStatus(WAITING_FOR_EXPIRATION_DATE);
        }
    }


    private ResponseHandler handleResponse(ProcessImageResponse response) {
        Optional<String> optionalBarcode;
        Optional<String> optionalExpirationDate;

        optionalBarcode = getBarcodeFrom(response);
        optionalExpirationDate = getExpirationDateFrom(response);

        return ResponseHandler.builder()
                .expirationDate(optionalExpirationDate.orElse(""))
                .barcode(optionalBarcode.orElse(""))
                .build();
    }

    private Optional<String> getExpirationDateFrom(ProcessImageResponse response) {
        String result = (String) response.getExpirationDate();
        if (result != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getBarcodeFrom(ProcessImageResponse response) {
        String result = "";
        if (response.getBarcode() != null) {
            result = response.getBarcode()[0].getData();
            return Optional.of(result);
        } else {
            return Optional.empty();
        }

    }

    private ProductScan getProductScan(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Optional<ProductScan> productScanOptional = productScanService.
                findProductScanByUserIdWithStatusWaitingBoth(userService.findUserById(update.getMessage().getFrom().getId()).orElseThrow());

        return productScanOptional.orElseThrow();
    }

}
