package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.request.CartAddRequest;
import com.example.fastcampusbookstore.dto.request.CartUpdateRequest;
import com.example.fastcampusbookstore.dto.response.CartSummaryResponse;
import com.example.fastcampusbookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CartController {

    private final CartService cartService;

    // 1. 장바구니 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartItems(HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("장바구니 조회 요청: memberId={}", memberId);

        try {
            CartSummaryResponse response = cartService.getCartSummary(memberId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("장바구니 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("장바구니 조회 중 오류가 발생했습니다."));
        }
    }

    // 2. 장바구니에 상품 추가
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addToCart(
            @Valid @RequestBody CartAddRequest request,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("장바구니 추가 요청: memberId={}, bookId={}, quantity={}",
                memberId, request.getBookId(), request.getQuantity());

        try {
            cartService.addToCart(memberId, request);
            return ResponseEntity.ok(ApiResponse.success("장바구니에 추가되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("장바구니 추가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("장바구니 추가 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("장바구니 추가 중 오류가 발생했습니다."));
        }
    }

    // 3. 장바구니 수량 수정
    @PutMapping("/{cartId}")
    public ResponseEntity<ApiResponse<String>> updateCartItem(
            @PathVariable Integer cartId,
            @Valid @RequestBody CartUpdateRequest request,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("장바구니 수정 요청: memberId={}, cartId={}, quantity={}",
                memberId, cartId, request.getQuantity());

        try {
            cartService.updateCartItem(memberId, cartId, request.getQuantity());
            return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("장바구니 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("장바구니 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("장바구니 수정 중 오류가 발생했습니다."));
        }
    }

    // 4. 장바구니 상품 삭제
    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiResponse<String>> removeCartItem(
            @PathVariable Integer cartId,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("장바구니 삭제 요청: memberId={}, cartId={}", memberId, cartId);

        try {
            cartService.removeCartItem(memberId, cartId);
            return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("장바구니 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("장바구니 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("장바구니 삭제 중 오류가 발생했습니다."));
        }
    }

    // 5. 선택한 상품들 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> removeSelectedItems(
            @RequestBody List<Integer> cartIds,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("선택 상품 삭제 요청: memberId={}, cartIds={}", memberId, cartIds);

        try {
            cartService.removeSelectedItems(memberId, cartIds);
            return ResponseEntity.ok(ApiResponse.success("선택한 상품들이 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("선택 상품 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("선택 상품 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("선택 상품 삭제 중 오류가 발생했습니다."));
        }
    }
}