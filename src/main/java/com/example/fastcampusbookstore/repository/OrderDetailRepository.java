package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Order;
import com.example.fastcampusbookstore.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // 주문별 주문 상세 조회
    List<OrderDetail> findByOrder(Order order);

    // 도서별 주문 상세 조회
    List<OrderDetail> findByBook(Book book);

    // 특정 주문의 특정 도서 주문 상세 조회
    List<OrderDetail> findByOrderAndBook(Order order, Book book);
}