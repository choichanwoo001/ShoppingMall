package com.example.fastcampusbookstore.dto.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;        // 데이터 목록
    private int currentPage;        // 현재 페이지
    private int totalPages;         // 전체 페이지 수
    private long totalElements;     // 전체 데이터 수
    private int size;              // 페이지 크기
    private boolean first;         // 첫 페이지 여부
    private boolean last;          // 마지막 페이지 여부
    private boolean hasNext;       // 다음 페이지 존재 여부
    private boolean hasPrevious;   // 이전 페이지 존재 여부

    public static <T> PageResponse<T> of(List<T> content, org.springframework.data.domain.Page<?> page) {
        PageResponse<T> response = new PageResponse<>();
        response.content = content;
        response.currentPage = page.getNumber();
        response.totalPages = page.getTotalPages();
        response.totalElements = page.getTotalElements();
        response.size = page.getSize();
        response.first = page.isFirst();
        response.last = page.isLast();
        response.hasNext = page.hasNext();
        response.hasPrevious = page.hasPrevious();
        return response;
    }
}
