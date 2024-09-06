package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class ProcessImageClient implements ProcessImageProxy{
    private final ProcessImageProxy processImageProxy;

    public ProcessImageClient(ProcessImageProxy processImageProxy) {
        this.processImageProxy = processImageProxy;
    }



    @Override
    public ProcessImageResponse fetchProcessImageResponse(Map<String, ?> form ) {
        ProcessImageResponse response = processImageProxy.fetchProcessImageResponse(form);
        log.info("We got response from process image service {}", response);
        return response;
    }
}
