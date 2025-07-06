package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventorySearchRequest {
    private String bookTitle;
    private String publisher;
    private String author;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sortBy; // bookTitle, price, registerDate
    private String sortOrder; // asc, desc
} 