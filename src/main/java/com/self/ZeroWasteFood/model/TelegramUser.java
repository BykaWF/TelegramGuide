package com.self.ZeroWasteFood.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Getter
@Setter
@Table(name = "\"user\"")
public class TelegramUser {

    @Id
    private Long id;

    private boolean isBot;

    private String firstName;

    private String lastName;

    private String username;

    private String languageCode;


    @OneToMany(mappedBy = "telegramUser", fetch = FetchType.LAZY)
    private List<TelegramProduct> telegramProductList;
    @OneToMany(mappedBy = "telegramUser", fetch = FetchType.LAZY)
    private List<ProductScan> productScans;


}
