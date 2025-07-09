package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.request.OrderCreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoPayService {
    @Value("${kakao.pay.admin-key}")
    private String adminKey;
    @Value("${kakao.pay.cid}")
    private String cid;
    @Value("${kakao.pay.approval-url}")
    private String approvalUrl;
    @Value("${kakao.pay.cancel-url}")
    private String cancelUrl;
    @Value("${kakao.pay.fail-url}")
    private String failUrl;

    // 카카오페이 결제 준비 (ready)
    public Map<String, String> kakaoPayReady(String memberId, OrderCreateRequest orderRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://kapi.kakao.com/v1/payment/ready";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        // partner_order_id를 고유값으로 생성
        String partnerOrderId = String.valueOf(System.currentTimeMillis());

        // 결제 요청 파라미터 세팅
        StringBuilder params = new StringBuilder();
        params.append("cid=").append(cid);
        params.append("&partner_order_id=").append(partnerOrderId);
        params.append("&partner_user_id=").append(memberId);
        params.append("&item_name=").append(orderRequest.getOrderItems().get(0).getBookId()); // 예시: 첫 상품명(실제는 상품명으로)
        params.append("&quantity=").append(orderRequest.getOrderItems().size());
        params.append("&total_amount=").append(orderRequest.getOrderItems().stream().mapToInt(i -> i.getQuantity() * 10000).sum()); // 예시: 금액(실제는 가격으로)
        params.append("&vat_amount=0");
        params.append("&tax_free_amount=0");
        params.append("&approval_url=").append(approvalUrl);
        params.append("&cancel_url=").append(cancelUrl);
        params.append("&fail_url=").append(failUrl);

        HttpEntity<String> entity = new HttpEntity<>(params.toString(), headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<String, Object> body = response.getBody();
        Map<String, String> result = new HashMap<>();
        result.put("tid", (String) body.get("tid"));
        result.put("next_redirect_pc_url", (String) body.get("next_redirect_pc_url"));
        result.put("partner_order_id", partnerOrderId);
        return result;
    }

    // 카카오페이 결제 승인 (approve)
    public Map<String, Object> kakaoPayApprove(String memberId, String tid, String partnerOrderId, String pgToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://kapi.kakao.com/v1/payment/approve";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        StringBuilder params = new StringBuilder();
        params.append("cid=").append(cid);
        params.append("&tid=").append(tid);
        params.append("&partner_order_id=").append(partnerOrderId);
        params.append("&partner_user_id=").append(memberId);
        params.append("&pg_token=").append(pgToken);

        HttpEntity<String> entity = new HttpEntity<>(params.toString(), headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }
} 