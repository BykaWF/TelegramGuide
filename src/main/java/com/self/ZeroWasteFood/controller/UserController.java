package com.self.ZeroWasteFood.controller;

import com.self.ZeroWasteFood.dto.CreateUserRequest;
import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(){
        List<TelegramUser> telegramUserList = userService.getUserList();
        log.info("We get userList : {}" , telegramUserList);
        return ResponseEntity.status(200).body(telegramUserList);
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@RequestBody CreateUserRequest createUserRequest){
        log.info("We get user {}", createUserRequest);
        userService.saveUser(createUserRequest.toUser());
        log.info("User was saved");
        return ResponseEntity.status(202).body("User was successfully added. " + createUserRequest);
    }



}
