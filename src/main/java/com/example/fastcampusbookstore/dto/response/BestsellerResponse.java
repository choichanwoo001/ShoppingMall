package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Bestseller;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BestsellerResponse {
    private Integer ranking;
    private Integer salesCount;
    private String targetMonth;

    // 도서 정보
    private Integer bookId;
    private String bookName;
    private String author;
    private String publisher;
    private BigDecimal price;
    private String bookImage;
    private BigDecimal rating;

    public static BestsellerResponse from(Bestseller bestseller) {
        BestsellerResponse response = new BestsellerResponse();
        response.ranking = bestseller.getRanking();
        response.salesCount = bestseller.getSalesCount();
        response.targetMonth = bestseller.getTargetMonth();

        if (bestseller.getBook() != null) {
            response.bookId = bestseller.getBook().getBookId();
            response.bookName = bestseller.getBook().getBookName();
            response.author = bestseller.getBook().getAuthor();
            response.publisher = bestseller.getBook().getPublisher();
            response.price = bestseller.getBook().getPrice();
            response.bookImage = bestseller.getBook().getBookImage();
            response.rating = bestseller.getBook().getRating();
        }

        return response;
    }
}
