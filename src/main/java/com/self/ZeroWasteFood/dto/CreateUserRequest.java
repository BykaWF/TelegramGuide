package com.self.ZeroWasteFood.dto;

import com.self.ZeroWasteFood.model.TelegramUser;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class CreateUserRequest {
    private String firstName;
    private String secondName;

    public TelegramUser toUser(){
        return TelegramUser.builder()
                .firstName(firstName)
                .lastName(secondName)
                .build();
    }
}
