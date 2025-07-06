package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bestsellers")
@Data
@EqualsAndHashCode(exclude = {"book"})
@ToString(exclude = {"book"})
public class Bestseller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bestseller_id")
    private Integer bestsellerId;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "sales_count")
    private Integer salesCount;

    @Column(name = "target_month", length = 7)
    private String targetMonth;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id2")
    private Book book;
}
