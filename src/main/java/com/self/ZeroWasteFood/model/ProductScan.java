package com.self.ZeroWasteFood.model;

import com.self.ZeroWasteFood.util.ScanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@ToString
@Table(name = "product_scan")
public class ProductScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode;

    private Date expirationDate;

    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUser telegramUser;


}
