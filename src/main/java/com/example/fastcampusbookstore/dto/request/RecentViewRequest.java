package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class RecentViewRequest {

    @NotNull(message = "도서 ID는 필수입니다")
    private Integer bookId;
}
