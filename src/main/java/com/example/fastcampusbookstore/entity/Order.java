package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Data
@EqualsAndHashCode(exclude = {"member", "orderDetails"})
@ToString(exclude = {"member", "orderDetails"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shipping_phone", length = 20)
    private String shippingPhone;

    @Column(name = "order_memo", columnDefinition = "TEXT")
    private String orderMemo;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id2")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Enum 정의
    public enum OrderStatus {
        주문완료, 결제완료, 배송준비, 배송중, 배송완료, 주문취소
    }

    public enum PaymentStatus {
        결제대기, 결제완료, 결제취소, 환불완료
    }
}
