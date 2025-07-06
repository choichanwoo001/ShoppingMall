package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Book;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BookListResponse {
    private Integer bookId;
    private String bookName;
    private String title;
    private String author;
    private String publisher;
    private BigDecimal price;
    private String bookImage;
    private String imageUrl;
    private BigDecimal rating;
    private String salesStatus;
    private String categoryName;
    private java.time.LocalDateTime registerDate;

    public static BookListResponse from(Book book) {
        BookListResponse response = new BookListResponse();
        response.bookId = book.getBookId();
        response.bookName = book.getBookName();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.publisher = book.getPublisher();
        response.price = book.getPrice();
        response.bookImage = book.getBookImage();
        response.imageUrl = book.getImageUrl();
        response.rating = book.getRating();
        response.salesStatus = book.getSalesStatus() != null ? book.getSalesStatus().toString() : null;
        response.categoryName = book.getCategory() != null ? book.getCategory().getCategoryName() : null;
        response.registerDate = book.getRegisterDate() != null ? book.getRegisterDate() : book.getRegistrationDate();
        return response;
    }

    public static List<BookListResponse> fromList(List<Book> books) {
        return books.stream()
                .map(BookListResponse::from)
                .collect(Collectors.toList());
    }
}
