package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Bestseller;
import com.example.fastcampusbookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BestsellerRepository extends JpaRepository<Bestseller, Integer> {

    // 특정 월의 베스트셀러 목록 (순위별 정렬)
    List<Bestseller> findByTargetMonthOrderByRanking(String targetMonth);

    // 특정 월, 특정 도서의 베스트셀러 정보
    Optional<Bestseller> findByTargetMonthAndBook(String targetMonth, Book book);

    // 특정 도서의 베스트셀러 이력
    List<Bestseller> findByBookOrderByTargetMonthDesc(Book book);

    // 특정 월의 베스트셀러 개수
    long countByTargetMonth(String targetMonth);

    // 모든 베스트셀러 목록 (순위별 정렬)
    List<Bestseller> findAllByOrderByRanking();
}