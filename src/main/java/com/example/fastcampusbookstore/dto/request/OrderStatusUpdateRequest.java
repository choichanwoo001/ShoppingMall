package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "주문 ID는 필수입니다")
    private Integer orderId;

    @NotBlank(message = "주문 상태는 필수입니다")
    private String orderStatus; // 주문완료, 결제완료, 배송준비, 배송중, 배송완료, 주문취소

    private String reason; // 취소/변경 사유 (선택사항)
}
