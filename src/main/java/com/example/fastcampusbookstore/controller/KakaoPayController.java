package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import com.example.fastcampusbookstore.service.KakaoPayService;
import com.example.fastcampusbookstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pay/kakao")
@RequiredArgsConstructor
@Slf4j
public class KakaoPayController {
    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;

    // 카카오페이 결제 준비 (ready)
    @PostMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, String>>> kakaoPayReady(@RequestBody OrderCreateRequest orderRequest,
                                                                         HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            Map<String, String> result = kakaoPayService.kakaoPayReady(memberId, orderRequest);
            session.setAttribute("kakaoPayTid", result.get("tid"));
            session.setAttribute("kakaoPayPartnerOrderId", result.get("partner_order_id"));
            session.setAttribute("orderRequest", orderRequest);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("카카오페이 결제 준비 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("카카오페이 결제 준비 중 오류가 발생했습니다."));
        }
    }

    // 카카오페이 결제 승인 (approve)
    @GetMapping("/approve")
    public ResponseEntity<Void> kakaoPayApprove(@RequestParam("pg_token") String pgToken, HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        String tid = (String) session.getAttribute("kakaoPayTid");
        String partnerOrderId = (String) session.getAttribute("kakaoPayPartnerOrderId");
        if (memberId == null || tid == null || partnerOrderId == null) {
            return ResponseEntity.status(302).header("Location", "/login").build();
        }
        try {
            Map<String, Object> result = kakaoPayService.kakaoPayApprove(memberId, tid, partnerOrderId, pgToken);
            // 결제 성공 시 주문 저장
            OrderCreateRequest orderRequest = (OrderCreateRequest) session.getAttribute("orderRequest");
            if (orderRequest != null) {
                orderService.createOrder(memberId, orderRequest);
                session.removeAttribute("orderRequest");
            }
            session.removeAttribute("kakaoPayTid");
            session.removeAttribute("kakaoPayPartnerOrderId");
            return ResponseEntity.status(302).header("Location", "/order/success").build();
        } catch (Exception e) {
            log.error("카카오페이 결제 승인 실패", e);
            return ResponseEntity.status(302).header("Location", "/order/fail").build();
        }
    }

    // 결제 취소
    @GetMapping("/cancel")
    public ResponseEntity<Void> kakaoPayCancel() {
        return ResponseEntity.status(302).header("Location", "/order/cancel").build();
    }

    // 결제 실패
    @GetMapping("/fail")
    public ResponseEntity<Void> kakaoPayFail() {
        return ResponseEntity.status(302).header("Location", "/order/fail").build();
    }
} 