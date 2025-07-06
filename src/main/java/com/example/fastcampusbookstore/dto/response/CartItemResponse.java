package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Cart;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Integer cartId;
    private Integer quantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedDate;

    // 도서 정보
    private BookInfo book;

    // 계산된 가격
    private BigDecimal totalPrice;

    @Data
    public static class BookInfo {
        private Integer bookId;
        private String bookName;
        private String author;
        private String publisher;
        private BigDecimal price;
        private String bookImage;
        private String salesStatus;
        private Integer stockQuantity; // 재고 수량
    }

    public static CartItemResponse from(Cart cart) {
        CartItemResponse response = new CartItemResponse();
        response.cartId = cart.getCartId();
        response.quantity = cart.getQuantity();
        response.addedDate = cart.getAddedDate();

        if (cart.getBook() != null) {
            BookInfo bookInfo = new BookInfo();
            bookInfo.bookId = cart.getBook().getBookId();
            bookInfo.bookName = cart.getBook().getBookName();
            bookInfo.author = cart.getBook().getAuthor();
            bookInfo.publisher = cart.getBook().getPublisher();
            bookInfo.price = cart.getBook().getPrice();
            bookInfo.bookImage = cart.getBook().getBookImage();
            bookInfo.salesStatus = cart.getBook().getSalesStatus() != null ?
                    cart.getBook().getSalesStatus().toString() : null;

            // 재고 정보
            if (cart.getBook().getInventory() != null) {
                bookInfo.stockQuantity = cart.getBook().getInventory().getStockQuantity();
            }

            response.book = bookInfo;

            // 총 가격 계산
            if (cart.getBook().getPrice() != null) {
                response.totalPrice = cart.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(cart.getQuantity()));
            }
        }

        return response;
    }
}
