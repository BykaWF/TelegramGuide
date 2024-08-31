package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.model.User;
import com.self.ZeroWasteFood.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public List<User> getUserList(){
        return userRepository.findAll();
    }

    public void addProductToUser(long chatId, String responseBody) {
    }
}
