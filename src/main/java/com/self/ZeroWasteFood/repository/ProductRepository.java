package com.self.ZeroWasteFood.repository;

import com.self.ZeroWasteFood.model.TelegramProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public  interface ProductRepository extends JpaRepository<TelegramProduct,Long> {
}
