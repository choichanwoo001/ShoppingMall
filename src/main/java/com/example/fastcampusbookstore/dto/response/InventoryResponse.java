package com.example.fastcampusbookstore.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryResponse {
    private Long bookId;
    private String isbn;
    private String bookName;
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    private String author;
    private String publisher;
    private Integer price;
    private Integer stockQuantity;
    private String salesStatus;
    private LocalDateTime registerDate;
    private String bookImage;
} 