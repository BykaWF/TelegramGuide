package com.self.ZeroWasteFood.repository;

import com.self.ZeroWasteFood.model.TelegramUser;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<TelegramUser, Long> {
    @Query("SELECT tu FROM TelegramUser tu LEFT JOIN FETCH tu.telegramProductList")
    List<TelegramUser> findAllWithProducts();
}
