package com.self.ZeroWasteFood.util;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseHandler {

    private String barcode;
    private String expirationDate;
}
