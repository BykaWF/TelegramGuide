package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.exception.NoProductByUserIdException;
import com.self.ZeroWasteFood.exception.NoUserByIdException;
import com.self.ZeroWasteFood.model.*;
import com.self.ZeroWasteFood.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    public void addProductToUserById(Long id, ProductScan productScan, ProductResponse productResponse){

        Optional<TelegramUser> userById = userService.findUserById(id);
        if(userById.isPresent()){
            TelegramUser telegramUser = userById.get();
            TelegramProduct product = getTelegramProduct(productScan,productResponse, telegramUser);
            telegramUser.getTelegramProductList().add(product);
            productRepository.save(product);
            userService.saveUser(telegramUser);
        }else {
            log.error("no such user, check is he in db");
        }
    }

    private TelegramProduct getTelegramProduct(ProductScan productScan,ProductResponse productResponse, TelegramUser telegramUser) {
        Product openFoodproduct = productResponse.getProduct();
        TelegramProduct product = new TelegramProduct();

        product.setProductName(openFoodproduct.getProductName());
        product.setTelegramUser(telegramUser);
        product.setNutritionGrades(openFoodproduct.getNutritionGrades());
        product.setImgUrl(openFoodproduct.getImageUrl());
        product.setExpirationDate(productScan.getExpirationDate());

        return product;
    }


    @Transactional
    public void addProductToUserById(Long id, ProductResponse productResponse, String expirationDate) throws NoUserByIdException, ParseException {
        Optional<TelegramUser> userById = userService.findUserById(id);
        log.info("We fetched userByID from user repo");

        if(userById.isPresent()){
            log.info("UserById is present and we started setting up product");
            TelegramUser telegramUser = userById.get();
            TelegramProduct product = getTelegramProduct(productResponse, expirationDate, telegramUser);
            telegramUser.getTelegramProductList().add(product);
            log.info("Append product list to user {} ", telegramUser.getFirstName());
            log.info("Product successfully added to db. {}", product.getProductName());
            productRepository.save(product);
            userService.saveUser(telegramUser);
        }else {
            throw new NoUserByIdException("We don't find user by id = " + id);
        }
    }

    private static @NotNull TelegramProduct getTelegramProduct(ProductResponse productResponse, String expirationDate, TelegramUser telegramUser) throws ParseException {
        Product openFoodproduct = productResponse.getProduct();
        TelegramProduct product = new TelegramProduct();

        product.setProductName(openFoodproduct.getProductName());
        product.setTelegramUser(telegramUser);
        product.setNutritionGrades(openFoodproduct.getNutritionGrades());
        product.setImgUrl(openFoodproduct.getImageUrl());

        if(expirationDate != null){
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
            Date date = sdf.parse(expirationDate);
            product.setExpirationDate(date);
        }
        return product;
    }

    public List<TelegramProduct> findByUserId(Long id){
        return productRepository.findProductByTelegramUserId(id);
    }
    @Transactional
    public void addExpirationDateToExistingProductById(Long id, String expirationData){
        log.info("We adding expiration date: {}. To existing product by id = {}", expirationData,id);
        List<TelegramProduct> productList = findByUserId(id);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
        try{

            if(!productList.isEmpty()){
                log.info("We find product by telegram user id");
                TelegramProduct product = productList.get(productList.size() - 1);
                Date date = sdf.parse(expirationData.contains("expiration_date") ? expirationData.substring(20, 28) : expirationData);
                product.setExpirationDate(date);
                log.info("We set expiration date to product");
                productRepository.save(product);
            }else {
                //TODO fix it we find list not nor product
                throw new NoProductByUserIdException("We don't find any product with id " + id);
            }
        }catch (ParseException e){
            log.info(e.getMessage());
        }

    }

}
