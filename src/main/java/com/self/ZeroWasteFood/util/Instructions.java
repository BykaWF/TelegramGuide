package com.self.ZeroWasteFood.util;

import com.vdurmont.emoji.EmojiParser;

public class Instructions {

    private Instructions(){}

    public static String barcodeUploadInstructions(String firstName) {
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                                %s ,please reply to this message and upload a clear photo of the barcode. 
                                                        
                                Ensure the barcode is well-lit and in focus for the best results.
                                """,
                        firstName
                )
        );
    }

    public static String productUploadInstructions(String firstName) {
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                                Hi, %s ! %s
                                                                
                                %s Please upload a photo of your product for us to scan the expiration date.
                                                                
                                %s Focus on the Date: The expiration date should be visible and centered in the photo.
                                """,
                        firstName,
                        EmojiParser.parseToUnicode(":wave:"),
                        EmojiParser.parseToUnicode(":calendar:"),
                        EmojiParser.parseToUnicode(":bulb:")
                )
        );
    }

}
