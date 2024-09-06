package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.dto.ProcessImageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "process-image-service", url = "http://127.0.0.1:5000")
public interface ProcessImageProxy {
    @PostMapping(value = "/process-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProcessImageResponse fetchProcessImageResponse( @RequestBody Map<String, ?> form);
}
