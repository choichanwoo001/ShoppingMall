package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.entity.RecentView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentView, Integer> {

    // 회원별 최근 본 상품 조회 (최신순, 제한된 개수)
    List<RecentView> findByMemberOrderByViewDateDesc(Member member, Pageable pageable);

    // 회원과 도서로 최근 본 상품 조회 (중복 확인용)
    Optional<RecentView> findByMemberAndBook(Member member, Book book);

    // 회원별 최근 본 상품 개수
    long countByMember(Member member);

    // 회원별 최근 본 상품 전체 삭제
    void deleteByMember(Member member);
}