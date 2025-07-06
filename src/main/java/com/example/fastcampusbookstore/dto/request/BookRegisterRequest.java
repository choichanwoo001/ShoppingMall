package com.example.fastcampusbookstore.dto.request;

import lombok.Data;

@Data
public class BookRegisterRequest {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private Integer price;
    private String imageUrl;
    private String pdfUrl;
    private String size;
    private Double rating;
    private Integer salesIndex;
    private String categoryTop;
    private String categoryMiddle;
    private String categoryBottom;
    private Integer stockQuantity;
    private String salesStatus; // 판매중, 절판, 일시품절, 입고예정
} 