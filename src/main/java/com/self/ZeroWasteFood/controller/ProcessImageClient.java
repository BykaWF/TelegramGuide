package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
public class ProcessImageClient implements ProcessImageProxy{
    private final ProcessImageProxy processImageProxy;

    public ProcessImageClient(ProcessImageProxy processImageProxy) {
        this.processImageProxy = processImageProxy;
    }


    @Override
    public ProcessImageResponse fetchProcessImageResponse(MultipartFile image, String type) {
        ProcessImageResponse response = processImageProxy.fetchProcessImageResponse(image,type);
        log.info("We got response from process image service {}", response);
        return response;
    }
}