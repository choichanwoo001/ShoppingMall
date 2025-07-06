package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.entity.OrderDetail;
import com.example.fastcampusbookstore.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 도서별 리뷰 조회 (최신순)
    Page<Review> findByBook(Book book, Pageable pageable);

    // 회원별 리뷰 조회 (최신순)
    Page<Review> findByMember(Member member, Pageable pageable);

    // 회원과 주문상세로 리뷰 존재 여부 확인
    boolean existsByMemberAndOrderDetail(Member member, OrderDetail orderDetail);

    // 특정 평점의 리뷰 조회
    List<Review> findByRating(Integer rating);

    // 추천 리뷰만 조회
    List<Review> findByIsRecommendedTrue();

    // 도서별 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double findAverageRatingByBook(@Param("book") Book book);

    // 도서별 리뷰 개수
    long countByBook(Book book);

    // 회원별 리뷰 개수
    long countByMember(Member member);
}