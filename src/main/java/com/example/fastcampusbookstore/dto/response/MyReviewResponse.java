package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Review;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class MyReviewResponse {
    private Integer reviewId;
    private Integer rating;
    private String reviewTitle;
    private String reviewContent;
    private Boolean isRecommended;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 도서 정보
    private BookInfo book;

    // 주문 정보
    private Integer orderDetailId;

    @Data
    public static class BookInfo {
        private Integer bookId;
        private String bookName;
        private String author;
        private String publisher;
        private String bookImage;
    }

    public static MyReviewResponse from(Review review) {
        MyReviewResponse response = new MyReviewResponse();
        response.reviewId = review.getReviewId();
        response.rating = review.getRating();
        response.reviewTitle = review.getReviewTitle();
        response.reviewContent = review.getReviewContent();
        response.isRecommended = review.getIsRecommended();
        response.createdAt = review.getCreatedAt();
        response.updatedAt = review.getUpdatedAt();

        // 도서 정보
        if (review.getBook() != null) {
            BookInfo bookInfo = new BookInfo();
            bookInfo.bookId = review.getBook().getBookId();
            bookInfo.bookName = review.getBook().getBookName();
            bookInfo.author = review.getBook().getAuthor();
            bookInfo.publisher = review.getBook().getPublisher();
            bookInfo.bookImage = review.getBook().getBookImage();
            response.book = bookInfo;
        }

        // 주문 정보
        if (review.getOrderDetail() != null) {
            response.orderDetailId = review.getOrderDetail().getOrderDetailId();
        }

        return response;
    }
}
