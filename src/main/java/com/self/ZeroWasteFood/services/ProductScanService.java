package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.repository.ProductScanRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.self.ZeroWasteFood.util.ScanStatus.COMPLETE;
import static com.self.ZeroWasteFood.util.ScanStatus.WAITING_FOR_BARCODE_AND_EXPIRATION_DATE;

@Slf4j
@Service
public class ProductScanService {
    private final ProductScanRepository productScanRepository;
    private final UserService userService;

    public ProductScanService(ProductScanRepository productScanRepository, UserService userService) {
        this.productScanRepository = productScanRepository;
        this.userService = userService;
    }

    /**
     * Created entry product scan for user at the begging of /full_scan
     *
     * @param telegramUser - to which one it would be connected
     */
    public void createProductScanWithUser(TelegramUser telegramUser) {
        ProductScan productScan = new ProductScan();
        productScan.setTelegramUser(telegramUser);
        productScan.setStatus(WAITING_FOR_BARCODE_AND_EXPIRATION_DATE);
        productScanRepository.save(productScan);
    }

    public Optional<ProductScan> findProductScanByUserIdWithStatusWaitingBoth(@NonNull TelegramUser telegramUser) {
        return productScanRepository.findByUserIdWithStatusWaitingBoth(telegramUser);
    }

    public void save(ProductScan productScan) {
        productScanRepository.save(productScan);
    }

    public boolean removeCompleteProductScan(ProductScan productScan) {
        if (productScan.getStatus().equals(COMPLETE)) {
            productScanRepository.delete(productScan);
            return true;
        }else {
            return false;
        }
    }
}
