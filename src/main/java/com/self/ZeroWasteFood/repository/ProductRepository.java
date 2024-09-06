package com.self.ZeroWasteFood.repository;

import com.self.ZeroWasteFood.model.TelegramProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public  interface ProductRepository extends JpaRepository<TelegramProduct,Long> {

   //@Query("SELECT tp FROM TelegramProduct tp WHERE tp.telegramUser = :user")
    List<TelegramProduct> findProductByTelegramUserId(Long id);
}
