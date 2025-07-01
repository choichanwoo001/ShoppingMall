package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import com.example.fastcampusbookstore.dto.response.OrderListResponse;
import com.example.fastcampusbookstore.dto.response.OrderResponse;
import com.example.fastcampusbookstore.entity.*;
import com.example.fastcampusbookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;

    // 1. 주문 생성
    @Transactional
    public OrderResponse createOrder(String memberId, OrderCreateRequest request) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // 주문 생성
        Order order = createOrderEntity(member, request);
        Order savedOrder = orderRepository.save(order);

        // 주문 상세 생성
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateRequest.OrderItem orderItem : request.getOrderItems()) {
            Book book = bookRepository.findById(orderItem.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

            // 판매 상태 및 재고 확인
            validateBookForOrder(book, orderItem.getQuantity());

            // 주문 상세 생성
            OrderDetail orderDetail = createOrderDetailEntity(savedOrder, book, orderItem.getQuantity());
            orderDetailRepository.save(orderDetail);

            totalAmount = totalAmount.add(orderDetail.getTotalPrice());

            // 장바구니에서 주문한 경우 해당 아이템 삭제
            if (orderItem.getCartId() != null) {
                cartRepository.deleteById(orderItem.getCartId());
            }
        }

        // 총 주문 금액 업데이트
        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        return OrderResponse.from(savedOrder);
    }

    // 2. 내 주문 목록 조회
    public PageResponse<OrderListResponse> getMyOrders(String memberId, int page, int size) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> orders = orderRepository.findByMember(member, pageable);

        List<OrderListResponse> orderResponses = orders.getContent().stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(orderResponses, orders);
    }

    // 3. 주문 상세 조회
    public OrderResponse getOrderDetail(String memberId, Integer orderId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다"));

        // 본인 주문인지 확인
        if (!order.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        return OrderResponse.from(order);
    }

    // 4. 주문 취소
    @Transactional
    public void cancelOrder(String memberId, Integer orderId, String reason) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다"));

        // 본인 주문인지 확인
        if (!order.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        // 취소 가능 상태인지 확인
        if (order.getOrderStatus() == Order.OrderStatus.배송완료 ||
                order.getOrderStatus() == Order.OrderStatus.주문취소) {
            throw new IllegalArgumentException("취소할 수 없는 주문 상태입니다");
        }

        // 주문 취소 처리
        order.setOrderStatus(Order.OrderStatus.주문취소);
        order.setOrderMemo(reason != null ? reason : "고객 요청에 의한 취소");
        orderRepository.save(order);
    }

    // === Private 헬퍼 메서드들 ===

    private Order createOrderEntity(Member member, OrderCreateRequest request) {
        Order order = new Order();
        order.setMember(member);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(Order.OrderStatus.주문완료);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.결제대기);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingPhone(request.getShippingPhone());
        order.setOrderMemo(request.getOrderMemo());
        return order;
    }

    private OrderDetail createOrderDetailEntity(Order order, Book book, Integer quantity) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setQuantity(quantity);
        orderDetail.setUnitPrice(book.getPrice());
        orderDetail.setTotalPrice(book.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return orderDetail;
    }

    private void validateBookForOrder(Book book, Integer quantity) {
        // 판매 상태 확인
        if (book.getSalesStatus() != Book.SalesStatus.판매중) {
            throw new IllegalArgumentException("현재 판매하지 않는 상품입니다: " + book.getBookName());
        }

        // 재고 확인
        if (book.getInventory() != null && book.getInventory().getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다: " + book.getBookName());
        }
    }
}