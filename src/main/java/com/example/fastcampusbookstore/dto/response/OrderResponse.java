package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Order;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {
    private Integer orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private String shippingAddress;
    private String shippingPhone;
    private String orderMemo;

    // 주문 상세 항목들
    private List<OrderDetailInfo> orderDetails;

    @Data
    public static class OrderDetailInfo {
        private Integer orderDetailId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        // 도서 정보
        private BookInfo book;

        @Data
        public static class BookInfo {
            private Integer bookId;
            private String bookName;
            private String author;
            private String publisher;
            private String bookImage;
        }
    }

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.orderId = order.getOrderId();
        response.orderDate = order.getOrderDate();
        response.totalAmount = order.getTotalAmount();
        response.orderStatus = order.getOrderStatus() != null ? order.getOrderStatus().toString() : null;
        response.paymentMethod = order.getPaymentMethod();
        response.paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : null;
        response.shippingAddress = order.getShippingAddress();
        response.shippingPhone = order.getShippingPhone();
        response.orderMemo = order.getOrderMemo();

        if (order.getOrderDetails() != null) {
            response.orderDetails = order.getOrderDetails().stream()
                    .map(detail -> {
                        OrderDetailInfo detailInfo = new OrderDetailInfo();
                        detailInfo.orderDetailId = detail.getOrderDetailId();
                        detailInfo.quantity = detail.getQuantity();
                        detailInfo.unitPrice = detail.getUnitPrice();
                        detailInfo.totalPrice = detail.getTotalPrice();

                        if (detail.getBook() != null) {
                            OrderDetailInfo.BookInfo bookInfo = new OrderDetailInfo.BookInfo();
                            bookInfo.bookId = detail.getBook().getBookId();
                            bookInfo.bookName = detail.getBook().getBookName();
                            bookInfo.author = detail.getBook().getAuthor();
                            bookInfo.publisher = detail.getBook().getPublisher();
                            bookInfo.bookImage = detail.getBook().getBookImage();
                            detailInfo.book = bookInfo;
                        }

                        return detailInfo;
                    })
                    .collect(Collectors.toList());
        }

        return response;
    }
}
