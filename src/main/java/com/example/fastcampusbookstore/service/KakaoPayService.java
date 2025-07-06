package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoPayService {
    // 카카오페이 결제 준비 (ready)
    public Map<String, String> kakaoPayReady(String memberId, OrderCreateRequest orderRequest) {
        // TODO: 카카오페이 결제 준비 API 연동 (다음 단계)
        // 예시: 결제창 URL, tid 등 반환
        throw new UnsupportedOperationException("카카오페이 결제 연동은 다음 단계에서 구현");
    }
} 