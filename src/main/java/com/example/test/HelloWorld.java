package com.example.test;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

public class HelloWorld {

    public static void main(String... args) throws Exception {
        Logger logger=Logger.getLogger(HelloWorld.class.getName());
        logger.info("This is a module-using Hello World!");

        TelegramBotsLongPollingApplication botApp = null;

        try{
            botApp = new TelegramBotsLongPollingApplication();
            String bitToken = "7496319396:AAGx2AE3USjrLNUJXDRB06EtZD8saLqspX0";
            botApp.registerBot(bitToken, new ZeroFoodWasteBot(bitToken));
            logger.info("Bot started");
            Thread.currentThread().join();
        } catch (TelegramApiException e) {
            logger.info("In main method at moment of register we caught exception " + e.getMessage());
        }finally {
            assert botApp != null;
            botApp.close();
        }
    }
}
