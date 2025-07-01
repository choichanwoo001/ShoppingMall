package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Book;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookDetailResponse {
    private Integer bookId;
    private String isbn;
    private String bookName;
    private String author;
    private String publisher;
    private BigDecimal price;
    private String bookSize;
    private String description;
    private String bookImage;
    private String previewPdf;
    private BigDecimal rating;
    private Integer salesIndex;
    private String salesStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    // 카테고리 정보
    private CategoryInfo category;

    // 재고 정보
    private Integer stockQuantity;

    // 리뷰 요약
    private ReviewSummary reviewSummary;

    @Data
    public static class CategoryInfo {
        private Integer categoryId;
        private String categoryName;
        private String middleCategoryName;
        private String topCategoryName;
    }

    @Data
    public static class ReviewSummary {
        private Long totalReviews;
        private BigDecimal averageRating;
        private Integer rating5Count;
        private Integer rating4Count;
        private Integer rating3Count;
        private Integer rating2Count;
        private Integer rating1Count;
    }

    public static BookDetailResponse from(Book book) {
        BookDetailResponse response = new BookDetailResponse();
        response.bookId = book.getBookId();
        response.isbn = book.getIsbn();
        response.bookName = book.getBookName();
        response.author = book.getAuthor();
        response.publisher = book.getPublisher();
        response.price = book.getPrice();
        response.bookSize = book.getBookSize();
        response.description = book.getDescription();
        response.bookImage = book.getBookImage();
        response.previewPdf = book.getPreviewPdf();
        response.rating = book.getRating();
        response.salesIndex = book.getSalesIndex();
        response.salesStatus = book.getSalesStatus() != null ? book.getSalesStatus().toString() : null;
        response.registrationDate = book.getRegistrationDate();

        // 카테고리 정보
        if (book.getCategory() != null) {
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.categoryId = book.getCategory().getCategoryId();
            categoryInfo.categoryName = book.getCategory().getCategoryName();
            if (book.getCategory().getMiddleCategory() != null) {
                categoryInfo.middleCategoryName = book.getCategory().getMiddleCategory().getCategoryName();
                if (book.getCategory().getMiddleCategory().getTopCategory() != null) {
                    categoryInfo.topCategoryName = book.getCategory().getMiddleCategory().getTopCategory().getCategoryName();
                }
            }
            response.category = categoryInfo;
        }

        // 재고 정보
        if (book.getInventory() != null) {
            response.stockQuantity = book.getInventory().getStockQuantity();
        }

        return response;
    }
}

