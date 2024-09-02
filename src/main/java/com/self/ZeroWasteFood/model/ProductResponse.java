package com.self.ZeroWasteFood.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductResponse {
    private Product product;

    private String code;

    private boolean status;

    @JsonProperty("status_verbose")
    private String statusVerbose;
}
