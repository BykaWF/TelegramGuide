package com.self.ZeroWasteFood.services;

import com.self.ZeroWasteFood.model.ProductScan;
import com.self.ZeroWasteFood.model.TelegramUser;
import com.self.ZeroWasteFood.repository.ProductScanRepository;
import com.self.ZeroWasteFood.util.ScanStatus;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.self.ZeroWasteFood.util.ScanStatus.WAITING_FOR_BARCODE_AND_EXPIRATION_DATE;

@Service
public class ProductScanService {
    private final ProductScanRepository productScanRepository;

    public ProductScanService(ProductScanRepository productScanRepository) {
        this.productScanRepository = productScanRepository;
    }

    /**
     * Created entry product scan for user at the begging of /full_scan
     * @param telegramUser - to which one it would be connected
     */
    public void createProductScanWithUser(TelegramUser telegramUser){
        ProductScan productScan = new ProductScan();
        productScan.setTelegramUser(telegramUser);
        productScan.setStatus(WAITING_FOR_BARCODE_AND_EXPIRATION_DATE);
        productScanRepository.save(productScan);
    }

    public Optional<ProductScan> findProductScanByUserIdWithStatusWaitingBoth(@NonNull Long id) {
        return productScanRepository.findByUserIdWithStatusWaitingBoth(id);
    }
}
