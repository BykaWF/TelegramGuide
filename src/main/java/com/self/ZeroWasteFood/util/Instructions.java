package com.self.ZeroWasteFood.util;

import com.vdurmont.emoji.EmojiParser;

public class Instructions {

    private Instructions(){}

    public static String barcodeUploadInstructions(String firstName) {
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                                Hi, %s! %s
                               \s
                                %s Please reply to this message and upload a clear photo of the barcode.\s
                                                       \s
                                %s Ensure the barcode is well-lit and in focus for the best results.
                               \s""",
                        firstName,
                        EmojiParser.parseToUnicode(":wave:"),
                        EmojiParser.parseToUnicode(":package:"),
                        EmojiParser.parseToUnicode(":bulb:")
                )
        );
    }

    public static String productUploadInstructions(String firstName) {
        return EmojiParser.parseToUnicode(
                String.format(
                        """
                                %s Please upload a photo of your product for us to scan the expiration date.

                                %s Focus on the Date: The expiration date should be visible and centered in the photo.
                               \s""",
                        EmojiParser.parseToUnicode(":calendar:"),
                        EmojiParser.parseToUnicode(":bulb:")
                )
        );
    }

    public static String registerNewUserInstruction(String firstName){
        return EmojiParser.parseToUnicode(String.format(
                """
               Hey %s! :blush:
               \s
               To help you us to manage your products better, we need to save you in our system.\s
               \s
               :sparkle: Just tap 'Add Me' below, and you're all set! 
               """,
                firstName
        ));
    }

    public static String infoInstructions(String firstName){
        return EmojiParser.parseToUnicode(String.format(
                """
               Hey %s! :smiley:
               \s
               To explore available commands, just click on the "Menu" button to the left of the chat:
               \s
                  📌 View available commands
                  📌 Access your profile settings
                  📌 Check out new features
               \s
               Feel free to aks anything!
               \s
               """,
                firstName
        ));
    }

}
