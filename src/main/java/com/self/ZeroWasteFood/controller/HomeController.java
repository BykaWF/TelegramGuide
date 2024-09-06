package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
@Slf4j
@Controller
public class HomeController {
    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/product-list/{userId}")
    public String getProductListPage(@PathVariable("userId") Long userId, Model model) {
        Optional<TelegramUser> userById = userService.findUserById(userId);
        log.info("We are in get mapping /product-list/{}", userId);
        if(userById.isPresent()){
            TelegramUser telegramUser = userById.get();
            model.addAttribute("productList",telegramUser.getTelegramProductList());
        }

        return "product-list";
    }
}
