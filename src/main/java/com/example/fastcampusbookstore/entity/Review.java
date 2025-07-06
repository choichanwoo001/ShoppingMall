package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Data
@EqualsAndHashCode(exclude = {"book", "member", "orderDetail"})
@ToString(exclude = {"book", "member", "orderDetail"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review_title", length = 200)
    private String reviewTitle;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name = "is_recommended")
    private Boolean isRecommended;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id2")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id2")
    private OrderDetail orderDetail;
}
