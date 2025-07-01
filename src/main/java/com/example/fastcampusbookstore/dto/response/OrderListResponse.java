package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Order;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListResponse {
    private Integer orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;

    // 대표 상품 정보 (첫 번째 상품)
    private String representativeBookName;
    private Integer totalItemCount; // 총 상품 종류 수

    public static OrderListResponse from(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.orderId = order.getOrderId();
        response.orderDate = order.getOrderDate();
        response.totalAmount = order.getTotalAmount();
        response.orderStatus = order.getOrderStatus() != null ? order.getOrderStatus().toString() : null;
        response.paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : null;

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            // 첫 번째 상품을 대표 상품으로 설정
            response.representativeBookName = order.getOrderDetails().get(0).getBook().getBookName();
            response.totalItemCount = order.getOrderDetails().size();
        }

        return response;
    }
}
