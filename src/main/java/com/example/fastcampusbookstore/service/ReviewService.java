package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.ReviewCreateRequest;
import com.example.fastcampusbookstore.dto.request.ReviewSearchRequest;
import com.example.fastcampusbookstore.dto.request.ReviewUpdateRequest;
import com.example.fastcampusbookstore.dto.response.MyReviewResponse;
import com.example.fastcampusbookstore.dto.response.ReviewResponse;
import com.example.fastcampusbookstore.entity.*;
import com.example.fastcampusbookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final OrderDetailRepository orderDetailRepository;

    // 1. 리뷰 작성
    @Transactional
    public void createReview(String memberId, ReviewCreateRequest request) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 상세입니다"));

        // 본인 주문인지 확인
        if (!orderDetail.getOrder().getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("본인이 주문한 상품만 리뷰 작성이 가능합니다");
        }

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByMemberAndOrderDetail(member, orderDetail)) {
            throw new IllegalArgumentException("이미 리뷰를 작성한 상품입니다");
        }

        // 리뷰 생성
        Review review = new Review();
        review.setBook(book);
        review.setMember(member);
        review.setOrderDetail(orderDetail);
        review.setRating(request.getRating());
        review.setReviewTitle(request.getReviewTitle());
        review.setReviewContent(request.getReviewContent());
        review.setIsRecommended(request.getIsRecommended());

        reviewRepository.save(review);
    }

    // 2. 상품별 리뷰 목록 조회
    public PageResponse<ReviewResponse> getBookReviews(ReviewSearchRequest request) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviews;

        if (request.getBookId() != null) {
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

            reviews = reviewRepository.findByBook(book, pageable);
        } else {
            reviews = reviewRepository.findAll(pageable);
        }

        List<ReviewResponse> reviewResponses = reviews.getContent().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(reviewResponses, reviews);
    }

    // 3. 내 리뷰 목록 조회
    public PageResponse<MyReviewResponse> getMyReviews(String memberId, int page, int size) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByMember(member, pageable);

        List<MyReviewResponse> reviewResponses = reviews.getContent().stream()
                .map(MyReviewResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(reviewResponses, reviews);
    }

    // 4. 리뷰 수정
    @Transactional
    public void updateReview(String memberId, ReviewUpdateRequest request) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다"));

        // 본인 리뷰인지 확인
        if (!review.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다");
        }

        // 리뷰 수정
        review.setRating(request.getRating());
        review.setReviewTitle(request.getReviewTitle());
        review.setReviewContent(request.getReviewContent());
        review.setIsRecommended(request.getIsRecommended());

        reviewRepository.save(review);
    }

    // 5. 리뷰 삭제
    @Transactional
    public void deleteReview(String memberId, Integer reviewId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다"));

        // 본인 리뷰인지 확인
        if (!review.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다");
        }

        reviewRepository.delete(review);
    }
}