package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Cart;
import com.example.fastcampusbookstore.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    // 회원별 장바구니 조회 (최근 추가순)
    List<Cart> findByMemberOrderByAddedDateDesc(Member member);

    // 회원과 도서로 장바구니 아이템 조회 (중복 확인용)
    Optional<Cart> findByMemberAndBook(Member member, Book book);

    // 회원별 장바구니 개수 조회
    long countByMember(Member member);

    // 회원별 장바구니 전체 삭제
    void deleteByMember(Member member);
}