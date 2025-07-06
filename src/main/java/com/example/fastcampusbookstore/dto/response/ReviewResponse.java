package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Review;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Integer reviewId;
    private Integer rating;
    private String reviewTitle;
    private String reviewContent;
    private Boolean isRecommended;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 작성자 정보 (마스킹 처리)
    private ReviewerInfo reviewer;

    // 도서 정보
    private BookInfo book;

    @Data
    public static class ReviewerInfo {
        private String memberName; // 마스킹된 이름 (예: 홍*동)
        private String memberGrade;
    }

    @Data
    public static class BookInfo {
        private Integer bookId;
        private String bookName;
        private String author;
        private String publisher;
        private String bookImage;
    }

    public static ReviewResponse from(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.reviewId = review.getReviewId();
        response.rating = review.getRating();
        response.reviewTitle = review.getReviewTitle();
        response.reviewContent = review.getReviewContent();
        response.isRecommended = review.getIsRecommended();
        response.createdAt = review.getCreatedAt();
        response.updatedAt = review.getUpdatedAt();

        // 작성자 정보 (마스킹 처리)
        if (review.getMember() != null) {
            ReviewerInfo reviewerInfo = new ReviewerInfo();
            reviewerInfo.memberName = maskName(review.getMember().getMemberName());
            reviewerInfo.memberGrade = review.getMember().getMemberGrade() != null ?
                    review.getMember().getMemberGrade().toString() : null;
            response.reviewer = reviewerInfo;
        }

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

        return response;
    }

    // 이름 마스킹 처리 (홍길동 → 홍*동)
    private static String maskName(String name) {
        if (name == null || name.length() <= 2) {
            return name;
        }

        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0)); // 첫 글자

        for (int i = 1; i < name.length() - 1; i++) {
            masked.append("*"); // 중간 글자들을 *로 마스킹
        }

        masked.append(name.charAt(name.length() - 1)); // 마지막 글자
        return masked.toString();
    }
}