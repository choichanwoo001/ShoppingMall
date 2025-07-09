package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.ReviewCreateRequest;
import com.example.fastcampusbookstore.dto.request.ReviewUpdateRequest;
import com.example.fastcampusbookstore.dto.response.MyReviewResponse;
import com.example.fastcampusbookstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.fastcampusbookstore.entity.OrderDetail;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.repository.MemberRepository;
import com.example.fastcampusbookstore.repository.OrderDetailRepository;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    // 내 리뷰 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<MyReviewResponse>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            PageResponse<MyReviewResponse> response = reviewService.getMyReviews(memberId, page, size);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("내 리뷰 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("내 리뷰 목록 조회 중 오류가 발생했습니다."));
        }
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Integer reviewId, HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            reviewService.deleteReview(memberId, reviewId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("리뷰 삭제 중 오류가 발생했습니다."));
        }
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(@PathVariable Integer reviewId, @RequestBody ReviewUpdateRequest request, HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
        }
        try {
            request.setReviewId(reviewId);
            reviewService.updateReview(memberId, request);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("리뷰 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("리뷰 수정 중 오류가 발생했습니다."));
        }
    }
}

// 리뷰 작성 폼 및 저장을 위한 Controller 추가
@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
class ReviewWriteController {
    private final ReviewService reviewService;
    private final MemberRepository memberRepository;
    private final OrderDetailRepository orderDetailRepository;

    // 리뷰 작성 폼
    @GetMapping("/write")
    public String showReviewForm(@RequestParam("bookId") Integer bookId, Model model, HttpSession session) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        Member member = memberRepository.findByMemberId(memberId).orElse(null);
        // 해당 회원이 구매한 orderDetail 중 bookId가 일치하는 것만 조회
        java.util.List<OrderDetail> orderDetailList = (member != null)
            ? orderDetailRepository.findAll().stream()
                .filter(od -> od.getBook() != null && od.getBook().getBookId().equals(bookId))
                .filter(od -> od.getOrder() != null && od.getOrder().getMember() != null && od.getOrder().getMember().getMemberId().equals(memberId))
                .toList()
            : java.util.Collections.emptyList();
        model.addAttribute("bookId", bookId);
        model.addAttribute("orderDetailList", orderDetailList);
        
        // 레이아웃 관련 속성 추가
        model.addAttribute("title", "리뷰 작성");
        model.addAttribute("cssFiles", java.util.List.of("review-write.css"));
        
        return "review-write";
    }

    // 리뷰 저장 처리
    @PostMapping("/write")
    public String submitReview(@ModelAttribute ReviewCreateRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }
        reviewService.createReview(memberId, request);
        redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다!");
        return "redirect:/product/" + request.getBookId();
    }
} 