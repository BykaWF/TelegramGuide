package com.self.ZeroWasteFood.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryUserStorage {

    private final ConcurrentHashMap<Long, User> storage;

    @Autowired
    public InMemoryUserStorage(ConcurrentHashMap<Long,User> storage) {
        this.storage = storage;
    }

    public void saveUser(Long key, User user){
        log.info("We store in memory user {}", user);
        storage.put(key,user);
    }

    public User getUserByKey(Long key){
        log.info("fetched user from storage");
        return storage.get(key);
    }
}
