package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.controller.OpenFoodFactsClient;
import com.self.ZeroWasteFood.controller.ProcessImageClient;
import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import com.self.ZeroWasteFood.model.ProductResponse;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ProcessImageService {
    private final ProcessImageClient processImageClient;
    private final ProductService productService;
    private final OpenFoodFactsClient openFoodFactsClient;

    public ProcessImageService(ProcessImageClient processImageClient, ProductService productService, OpenFoodFactsClient openFoodFactsClient) {
        this.processImageClient = processImageClient;
        this.productService = productService;
        this.openFoodFactsClient = openFoodFactsClient;
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

            log.info("Status code : {}", response.getStatusLine().getStatusCode());

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
     *  Use when we have barcode and expiration date on the picture.
     * @param img image from chat
     * @param update represent updates from chat
     */
    public void getProcessImageResponseAndAddProductToUser(File img, Update update) {
        Map<String,File> form = new HashMap<>();
        form.put("image",img);
        ProcessImageResponse response = processImageClient.fetchProcessImageResponse(form);
        log.info("We have fetched response {}", response);

        String barcode = response.getBarcode()[0].getData();
        String expirationDate = (String) response.getExpirationDate();

        ProductResponse productResponse = openFoodFactsClient.fetchProductByCode(barcode);
        log.info("Product name {}", productResponse.getProduct().getProductName());
        try {
            productService.addProductToUserById(update.getMessage().getChat().getId(), productResponse, expirationDate);
        }catch (ParseException e){
            log.info(e.getMessage());
        }

    }

}
