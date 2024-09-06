package com.self.ZeroWasteFood.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProcessImageResponse {

    @JsonProperty("barcode")
    private Barcode[] barcode;

    @JsonProperty("expiration_date")
    private Object expirationDate;
}
