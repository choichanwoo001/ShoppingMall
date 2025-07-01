package com.example.fastcampusbookstore.dto.common;

import lombok.Data;

@Data
public class PageRequest {
    private int page = 0;          // 페이지 번호 (0부터 시작)
    private int size = 30;         // 페이지 크기 (기본 30)
    private String sort;           // 정렬 필드
    private String direction = "asc"; // 정렬 방향 (asc, desc)

    public PageRequest() {}

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageRequest(int page, int size, String sort, String direction) {
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
    }
}
