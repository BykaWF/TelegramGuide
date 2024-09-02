package com.self.ZeroWasteFood.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@ComponentScan
public class ProjectConfig {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramClient telegramClient(){
        return new OkHttpTelegramClient(botToken);
    }
}
