package com.self.ZeroWasteFood.services;

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

@Slf4j
@Service
public class ExpirationDateExtractionService {

    public String buildPostRequest(File img, long chatId, String fileId, Update update) throws IOException {
        HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/process-image");

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
}
