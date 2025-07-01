package com.example.fastcampusbookstore.dto.request;

import com.example.fastcampusbookstore.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewSearchRequest extends PageRequest {
    private Integer bookId;       // 특정 도서의 리뷰 조회
    private Integer rating;       // 평점별 필터링
    private Boolean isRecommended; // 추천 여부 필터링

    public ReviewSearchRequest() {
        super();
    }

    public ReviewSearchRequest(int page, int size) {
        super(page, size);
    }
}
