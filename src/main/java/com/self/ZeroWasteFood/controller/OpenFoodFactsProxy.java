package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.model.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "open-food-facts", url = "${openfoodfacts.api.url}")
public interface OpenFoodFactsProxy {

    @GetMapping("/product/{code}.json")
    ProductResponse fetchProductByCode(@PathVariable("code") String code);

}
