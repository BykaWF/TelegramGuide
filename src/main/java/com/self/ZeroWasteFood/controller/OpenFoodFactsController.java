package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.model.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OpenFoodFactsController {
    private final OpenFoodFactsProxy openFoodFactsProxy;

    public OpenFoodFactsController(OpenFoodFactsProxy openFoodFactsProxy) {
        this.openFoodFactsProxy = openFoodFactsProxy;
    }

    @GetMapping("/product/{code}.json")
    public ProductResponse fetchProductByCode(@PathVariable("code") String code) {
        ProductResponse productResponse = openFoodFactsProxy.fetchProductByCode(code);
        log.info("Product: {} ", productResponse);
        return productResponse;
    }
}
