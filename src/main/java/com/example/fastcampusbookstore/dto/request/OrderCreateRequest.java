package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotEmpty(message = "주문 상품은 필수입니다")
    private List<OrderItem> orderItems;

    @NotBlank(message = "배송지 주소는 필수입니다")
    private String shippingAddress;

    @NotBlank(message = "배송지 연락처는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-0000-0000 형식이어야 합니다")
    private String shippingPhone;

    private String orderMemo; // 주문 메모 (선택사항)

    @NotBlank(message = "결제 방법은 필수입니다")
    private String paymentMethod; // 결제 방법

    @Data
    public static class OrderItem {
        @NotNull(message = "도서 ID는 필수입니다")
        private Integer bookId;

        @NotNull(message = "수량은 필수입니다")
        private Integer quantity;

        private Integer cartId; // 장바구니에서 주문하는 경우
    }
}
