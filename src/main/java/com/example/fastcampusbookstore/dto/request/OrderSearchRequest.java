package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderSearchRequest {
    private String ordererName;
    private String bookName;
    private String publisher;
    private String author;
    private String salesStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sortBy; // orderDate, ordererId, totalAmount, orderStatus
    private String sortOrder; // asc, desc
} 