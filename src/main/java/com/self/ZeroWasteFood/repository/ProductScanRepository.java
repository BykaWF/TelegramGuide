package com.self.ZeroWasteFood.repository;

import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductScanRepository extends JpaRepository<ProductScan,Long> {

    @Query("SELECT ps FROM ProductScan ps WHERE ps.telegramUser = :userId AND ps.status = 'WAITING_FOR_BARCODE_AND_EXPIRATION_DATE'")
    Optional<ProductScan> findByUserIdWithStatusWaitingBoth(@Param("userId") TelegramUser telegramUser);

}
