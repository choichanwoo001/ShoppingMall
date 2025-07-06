package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.response.RecentViewResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.entity.RecentView;
import com.example.fastcampusbookstore.repository.BookRepository;
import com.example.fastcampusbookstore.repository.MemberRepository;
import com.example.fastcampusbookstore.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewService {

    private final RecentViewRepository recentViewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private static final Logger log = LoggerFactory.getLogger(RecentViewService.class);

    /**
     * 최근 본 상품 목록 조회
     */
    public List<RecentViewResponse> getRecentViews(String memberId, int limit) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Pageable pageable = PageRequest.of(0, limit);
        List<RecentView> recentViews = recentViewRepository.findByMemberOrderByViewDateDesc(member, pageable);

        return recentViews.stream()
                .map(RecentViewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 본 상품 추가
     */
    @Transactional
    public void addRecentView(String memberId, Integer bookId) {
        log.info("[RecentViewService] addRecentView 호출 - memberId={}, bookId={}", memberId, bookId);
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

        // 기존에 본 기록이 있는지 확인
        Optional<RecentView> existingView = recentViewRepository.findByMemberAndBook(member, book);

        if (existingView.isPresent()) {
            // 기존 기록이 있으면 조회 시간만 업데이트
            RecentView recentView = existingView.get();
            recentView.setViewDate(LocalDateTime.now());
            recentViewRepository.save(recentView);
            log.info("[RecentViewService] 기존 기록 업데이트 - viewId={}, memberId={}, bookId={}", recentView.getViewId(), memberId, bookId);
        } else {
            // 새로운 조회 기록 추가
            RecentView recentView = new RecentView();
            recentView.setMember(member);
            recentView.setBook(book);
            recentView.setViewDate(LocalDateTime.now());
            recentViewRepository.save(recentView);
            log.info("[RecentViewService] 새 기록 추가 - memberId={}, bookId={}", memberId, bookId);

            // 최근 본 상품이 10개를 초과하면 가장 오래된 것 삭제
            cleanupOldRecentViews(member);
        }
    }

    /**
     * 오래된 최근 본 상품 정리 (최대 10개 유지)
     */
    @Transactional
    public void cleanupOldRecentViews(Member member) {
        long totalCount = recentViewRepository.countByMember(member);

        if (totalCount > 10) {
            Pageable pageable = PageRequest.of(10, (int)(totalCount - 10));
            List<RecentView> oldViews = recentViewRepository.findByMemberOrderByViewDateDesc(member, pageable);
            recentViewRepository.deleteAll(oldViews);
        }
    }

    /**
     * 회원의 모든 최근 본 상품 삭제
     */
    @Transactional
    public void clearAllRecentViews(String memberId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        recentViewRepository.deleteByMember(member);
    }

    /**
     * 특정 최근 본 상품 삭제
     */
    @Transactional
    public void removeRecentView(String memberId, Integer bookId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

        Optional<RecentView> recentView = recentViewRepository.findByMemberAndBook(member, book);
        recentView.ifPresent(recentViewRepository::delete);
    }
}