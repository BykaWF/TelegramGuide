package com.self.ZeroWasteFood.dto;

import com.self.ZeroWasteFood.model.User;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class CreateUserRequest {
    private String firstName;
    private String secondName;

    public User toUser(){
        return User.builder()
                .firstName(firstName)
                .secondName(secondName)
                .build();
    }
}
