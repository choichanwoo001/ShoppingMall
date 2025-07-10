
// OrderRepository.java
package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    // 회원별 주문 목록 조회 (최신순)
    Page<Order> findByMember(Member member, Pageable pageable);

    // 주문 상태별 조회
    List<Order> findByOrderStatus(Order.OrderStatus orderStatus);
    Page<Order> findByOrderStatus(Order.OrderStatus orderStatus, Pageable pageable);

    // 결제 상태별 조회
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);
    Page<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable);

    // 주문일 범위로 조회
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // 회원별 주문 개수
    long countByMember(Member member);

    // 특정 회원의 특정 상태 주문 조회
    List<Order> findByMemberAndOrderStatus(Member member, Order.OrderStatus orderStatus);

    long countByOrderDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}