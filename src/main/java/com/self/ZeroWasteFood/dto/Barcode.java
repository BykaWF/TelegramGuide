package com.self.ZeroWasteFood.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Barcode {
    @JsonProperty("data")
    private String data;
}
