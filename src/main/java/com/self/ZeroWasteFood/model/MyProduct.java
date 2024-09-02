package com.self.ZeroWasteFood.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "product")
public class MyProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productName;
    private Date experationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

}
