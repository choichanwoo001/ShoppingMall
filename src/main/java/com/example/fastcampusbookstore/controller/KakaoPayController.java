package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import com.example.fastcampusbookstore.service.KakaoPayService;
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
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("카카오페이 결제 준비 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("카카오페이 결제 준비 중 오류가 발생했습니다."));
        }
    }

    // TODO: 결제 승인/실패 콜백 엔드포인트 추가 예정
} 