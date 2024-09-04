package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(TelegramUser telegramUser){
        userRepository.save(telegramUser);
    }

    public void addUserAsTelegramUser(User user){

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setId(user.getId());
        telegramUser.setUsername(user.getUserName());
        telegramUser.setFirstName(user.getFirstName());
        telegramUser.setLastName(user.getLastName());
        telegramUser.setLanguageCode(user.getLanguageCode());
        telegramUser.setBot(user.getIsBot());
        userRepository.save(telegramUser);
    }


    @Transactional
    public List<TelegramUser> getUserList(){
        return userRepository.findAllWithProducts();
    }

    public Optional<TelegramUser> findUserById(Long id){
        return userRepository.findUserById(id);
    }

    public void addProductToUser(long chatId, String responseBody, Update update) {
        if(userRepository.findById(update.getMessage().getChat().getId()).isPresent()){
            // create product
            // save it product repo
            // attach product to user
            // save user
        }
        // create user
        // save it to product repo
        // attach to user
        // save user
    }
}
