package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.request.RecentViewRequest;
import com.example.fastcampusbookstore.dto.response.RecentViewResponse;
import com.example.fastcampusbookstore.service.RecentViewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/recent-views")
public class RecentViewController {
    private final RecentViewService recentViewService;

    // 최근 본 상품 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addRecentView(@Validated @RequestBody RecentViewRequest request,
                                                          HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            log.warn("[RecentView] 로그인 필요 - 최근 본 상품 추가 실패 (bookId={})", request.getBookId());
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            recentViewService.addRecentView(memberId, request.getBookId());
            log.info("[RecentView] 저장 성공 - memberId={}, bookId={}", memberId, request.getBookId());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("[RecentView] 저장 실패 - memberId={}, bookId={}, error={}", memberId, request.getBookId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("최근 본 상품 저장에 실패했습니다."));
        }
    }

    // 최근 본 상품 목록 조회 (최신순, 최대 10개)
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecentViewResponse>>> getRecentViews(HttpSession session,
                                                                               @RequestParam(defaultValue = "10") int limit) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        List<RecentViewResponse> result = recentViewService.getRecentViews(memberId, limit);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 최근 본 상품 개별 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeRecentView(@PathVariable Integer bookId, HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        recentViewService.removeRecentView(memberId, bookId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 최근 본 상품 전체 삭제
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearAllRecentViews(HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        recentViewService.clearAllRecentViews(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 