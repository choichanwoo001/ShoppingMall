package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inventory")
@Data
@EqualsAndHashCode(exclude = {"book"})
@ToString(exclude = {"book"})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id2")
    private Book book;
}
