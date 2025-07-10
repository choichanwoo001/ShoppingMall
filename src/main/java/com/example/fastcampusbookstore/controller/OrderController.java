package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.KakaoPayReadyRequest;
import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import com.example.fastcampusbookstore.dto.response.KakaoPayReadyResponse;
import com.example.fastcampusbookstore.dto.response.OrderListResponse;
import com.example.fastcampusbookstore.dto.response.OrderResponse;
import com.example.fastcampusbookstore.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

// OrderController, KakaoPayController의 모든 메서드와 의존성 주입을 이 파일로 합친다.
// 클래스명: OrderApiController
// @RestController, @RequestMapping 등은 그대로 유지
// KakaoPayController의 메서드와 의존성 주입을 이 클래스에 추가
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {

    private final OrderPaymentService orderPaymentService;

    // 1. 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("주문 생성 요청: memberId={}, itemCount={}",
                memberId, request.getOrderItems().size());

        try {
            OrderResponse response = orderPaymentService.createOrder(memberId, request);
            log.info("주문 생성 완료: orderId={}", response.getOrderId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("주문이 완료되었습니다.", response));

        } catch (IllegalArgumentException e) {
            log.warn("주문 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("주문 생성 중 오류가 발생했습니다."));
        }
    }

    // 2. 내 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("주문 목록 조회 요청: memberId={}, page={}, size={}",
                memberId, page, size);

        try {
            PageResponse<OrderListResponse> response = orderPaymentService.getMyOrders(memberId, page, size);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("주문 목록 조회 중 오류가 발생했습니다."));
        }
    }

    // 3. 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Integer orderId,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("주문 상세 조회 요청: memberId={}, orderId={}", memberId, orderId);

        try {
            OrderResponse response = orderPaymentService.getOrderDetail(memberId, orderId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("주문 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("주문 상세 조회 중 오류가 발생했습니다."));
        }
    }

    // 4. 주문 취소
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestBody(required = false) String reason,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("주문 취소 요청: memberId={}, orderId={}, reason={}",
                memberId, orderId, reason);

        try {
            orderPaymentService.cancelOrder(memberId, orderId, reason);
            return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("주문 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("주문 취소 중 오류가 발생했습니다."));
        }
    }

    // 카카오페이 결제 준비
    @PostMapping("/api/pay/kakao/ready")
    public ResponseEntity<ApiResponse<KakaoPayReadyResponse>> kakaoPayReady(
            @RequestBody KakaoPayReadyRequest request,
            HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            KakaoPayReadyResponse response = orderPaymentService.kakaoPayReady(memberId, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("카카오페이 결제 준비 중 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("카카오페이 결제 준비 중 오류가 발생했습니다."));
        }
    }
}