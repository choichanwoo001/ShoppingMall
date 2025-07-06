package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "도서 ID는 필수입니다")
    private Integer bookId;

    @NotNull(message = "주문 상세 ID는 필수입니다")
    private Integer orderDetailId;

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private Integer rating;

    @NotBlank(message = "리뷰 제목은 필수입니다")
    @Size(max = 200, message = "리뷰 제목은 200자 이하여야 합니다")
    private String reviewTitle;

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(max = 2000, message = "리뷰 내용은 2000자 이하여야 합니다")
    private String reviewContent;

    private Boolean isRecommended; // 추천 여부 (선택사항)
}
