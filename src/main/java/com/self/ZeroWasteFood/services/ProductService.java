package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.exception.NoUserByIdException;
import com.self.ZeroWasteFood.model.Product;
import com.self.ZeroWasteFood.model.ProductResponse;
import com.self.ZeroWasteFood.model.TelegramProduct;
import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public ProductService(ProductRepository productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Transactional
    public void addProductToUserById(Long id, ProductResponse productResponse) throws NoUserByIdException{
        Optional<TelegramUser> userById = userService.findUserById(id);
        log.info("We fetched userByID from user repo");

        if(userById.isPresent()){
            log.info("UserById is present and we started setting up product");
            TelegramUser telegramUser = userById.get();
            Product openFoodproduct = productResponse.getProduct();
            TelegramProduct product = new TelegramProduct();

            product.setProductName(openFoodproduct.getProductName());
            product.setTelegramUser(telegramUser);
            product.setNutritionGrades(openFoodproduct.getNutritionGrades());
            product.setImgUrl(openFoodproduct.getImageUrl());

            //TODO add this to product list of user.
            log.info("Product successfully added to db. {}", product);
            productRepository.save(product);
        }else {
            throw new NoUserByIdException("We don't find user by id = " + id);
        }
    }

    public void addExpirationDateToExistingProduct(){
        //TODO think how to implement it
    }

}
