package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.RecentView;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecentViewResponse {
    private Integer viewId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime viewDate;

    // 도서 정보
    private BookInfo book;

    @Data
    public static class BookInfo {
        private Integer bookId;
        private String bookName;
        private String author;
        private String publisher;
        private BigDecimal price;
        private String bookImage;
        private BigDecimal rating;
        private String salesStatus;
    }

    public static RecentViewResponse from(RecentView recentView) {
        RecentViewResponse response = new RecentViewResponse();
        response.viewId = recentView.getViewId();
        response.viewDate = recentView.getViewDate();

        if (recentView.getBook() != null) {
            BookInfo bookInfo = new BookInfo();
            bookInfo.bookId = recentView.getBook().getBookId();
            bookInfo.bookName = recentView.getBook().getBookName();
            bookInfo.author = recentView.getBook().getAuthor();
            bookInfo.publisher = recentView.getBook().getPublisher();
            bookInfo.price = recentView.getBook().getPrice();
            bookInfo.bookImage = recentView.getBook().getBookImage();
            bookInfo.rating = recentView.getBook().getRating();
            bookInfo.salesStatus = recentView.getBook().getSalesStatus() != null ?
                    recentView.getBook().getSalesStatus().toString() : null;
            response.book = bookInfo;
        }

        return response;
    }
}
