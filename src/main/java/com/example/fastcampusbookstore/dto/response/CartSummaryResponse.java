package com.example.fastcampusbookstore.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartSummaryResponse {
    private List<CartItemResponse> items;
    private Integer totalItems;         // 총 상품 종류 수
    private Integer totalQuantity;      // 총 수량
    private BigDecimal subtotalAmount;  // 상품 금액 합계
    private BigDecimal shippingCost;    // 배송비
    private BigDecimal totalAmount;     // 총 결제 금액
    private boolean freeShipping;       // 무료배송 여부

    public static CartSummaryResponse from(List<CartItemResponse> items) {
        CartSummaryResponse response = new CartSummaryResponse();
        response.items = items;
        response.totalItems = items.size();

        // 합계 계산
        response.totalQuantity = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        response.subtotalAmount = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 배송비 계산 (3만원 이상 무료배송)
        BigDecimal freeShippingThreshold = new BigDecimal("30000");
        response.freeShipping = response.subtotalAmount.compareTo(freeShippingThreshold) >= 0;
        response.shippingCost = response.freeShipping ? BigDecimal.ZERO : new BigDecimal("2500");

        response.totalAmount = response.subtotalAmount.add(response.shippingCost);

        return response;
    }
}
