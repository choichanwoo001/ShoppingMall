package com.example.fastcampusbookstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryResponse {
    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer price;
    private Integer stockQuantity;
    private String salesStatus;
    private LocalDateTime registerDate;
    private String imageUrl;
} 