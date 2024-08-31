package com.self.ZeroWasteFood.model;

import jakarta.persistence.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.*;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productName;
    private Date experationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

}
