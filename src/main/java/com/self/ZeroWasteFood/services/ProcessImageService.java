package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.controller.OpenFoodFactsClient;
import com.self.ZeroWasteFood.controller.ProcessImageClient;
import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import com.self.ZeroWasteFood.exception.NoBarcodeDetectedException;
import com.self.ZeroWasteFood.exception.NoExpirationDateDetectedException;
import com.self.ZeroWasteFood.exception.NoSuchProductException;
import com.self.ZeroWasteFood.model.ProductResponse;
import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.util.FileMultipart;
import feign.FeignException;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.self.ZeroWasteFood.util.ProcessImageRequestType.FULL_SCAN;

@Slf4j
@Service
public class ProcessImageService {
    private final ProcessImageClient processImageClient;
    private final ProductService productService;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final ProductScanService productScanService;
    private final MessageService messageService;

    public ProcessImageService(ProcessImageClient processImageClient, ProductService productService, OpenFoodFactsClient openFoodFactsClient, ProductScanService productScanService, MessageService messageService) {
        this.processImageClient = processImageClient;
        this.productService = productService;
        this.openFoodFactsClient = openFoodFactsClient;
        this.productScanService = productScanService;
        this.messageService = messageService;
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
        //TODO fetch the product by waiting for and so on
        String barcode = "";
        String expirationDate = "";
        try {
            ProcessImageResponse response = processImageClient.fetchProcessImageResponse(new FileMultipart(img.toPath()), String.valueOf(FULL_SCAN));
            ProductScan productScan = getProductScan(update);

            log.info("We have fetched response {}", response);

            barcode = getBarcodeFrom(response);
            expirationDate = getExpirationDateFrom(response);
            productScan.setBarcode(barcode);
            productScan.setExpirationDate(new SimpleDateFormat("dd.MM.yy").parse(expirationDate));

            ProductResponse productResponse = openFoodFactsClient.fetchProductByCode(barcode);
            log.info("Product name {}", productResponse.getProduct().getProductName());

        } catch (NoSuchElementException e) {
            log.error("We don't have product scan by user id that waiting for expiration date and barcode");
            log.error(e.getMessage());
        } catch (FeignException.BadRequest badRequest) {
            log.error("Bad Request: {}", badRequest.getMessage());
            messageService.sendTextMessage(update.getMessage().getChatId(), "Occurred server error. Try again later!");
        } catch (NoSuchProductException productException) {
            log.error("We can't fetch product from Open Food API with barcode {}", barcode);
            messageService.sendTextMessage(update.getMessage().getChatId(), "We unable find in our base your product. Sorry :(");
        } catch (NoBarcodeDetectedException e) {
            // TODO manually or try again
        } catch (NoExpirationDateDetectedException e) {
            // TODO manually or try again
            e.getMessage();
        } catch (ParseException e) {
            log.error("Unable to parse expiration date result {}", expirationDate);
            //TODO we can't understand this expiration date, manually or try again.
        }


    }

    private String getExpirationDateFrom(ProcessImageResponse response) {
        String result = (String) response.getExpirationDate();
        if (result != null) {
            return result;
        } else {
            log.error("We can't detect expiration date on image");
            throw new NoExpirationDateDetectedException("Expiration date want's detected on image");
        }
    }

    private String getBarcodeFrom(ProcessImageResponse response) {
        String result = "";
        if (response.getBarcode() != null) {
            result = response.getBarcode()[0].getData();
            return result;
        } else {
            log.error("Barcode is null");
            throw new NoBarcodeDetectedException("Barcode wasn't detected on image");
        }

    }

    private ProductScan getProductScan(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Optional<ProductScan> productScanOptional = productScanService.
                findProductScanByUserIdWithStatusWaitingBoth(update.getMessage().getFrom().getId());

        return productScanOptional.orElseThrow();
    }

}
