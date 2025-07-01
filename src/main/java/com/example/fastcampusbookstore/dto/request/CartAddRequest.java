package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class CartAddRequest {

    @NotNull(message = "도서 ID는 필수입니다")
    private Integer bookId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;
}
