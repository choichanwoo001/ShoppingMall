package com.example.fastcampusbookstore.dto.request;

import com.example.fastcampusbookstore.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookSearchRequest extends PageRequest {
    private String keyword;        // 통합 검색어 (책이름, 저자, 출판사)
    private String bookName;       // 책이름
    private String author;         // 저자
    private String publisher;      // 출판사
    private Integer categoryId;    // 카테고리 ID
    private String salesStatus;    // 판매상태
    private String minPrice;       // 최소 가격
    private String maxPrice;       // 최대 가격

    public BookSearchRequest() {
        super();
    }

    public BookSearchRequest(int page, int size) {
        super(page, size);
    }
}
