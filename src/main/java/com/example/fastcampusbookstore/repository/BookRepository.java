package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.CategoryBottom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    // === 기본 조회 ===

    // ISBN으로 도서 조회
    Optional<Book> findByIsbn(String isbn);

    // 도서명으로 조회 (정확히 일치)
    Optional<Book> findByBookName(String bookName);

    // 판매 상태별 조회
    List<Book> findBySalesStatus(Book.SalesStatus salesStatus);

    Page<Book> findBySalesStatus(Book.SalesStatus salesStatus, Pageable pageable);

    // 카테고리별 조회
    List<Book> findByCategory(CategoryBottom category);

    Page<Book> findByCategory(CategoryBottom category, Pageable pageable);

    // === 검색 기능 (사용자 요구사항: 책이름, 출판사, 저자로 검색) ===

    // 통합 검색 - 책이름, 출판사, 저자에서 키워드 검색
    @Query("SELECT b FROM Book b WHERE " +
            "b.bookName LIKE %:keyword% OR " +
            "b.author LIKE %:keyword% OR " +
            "b.publisher LIKE %:keyword%")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 책이름으로 검색 (부분 일치)
    Page<Book> findByBookNameContaining(String bookName, Pageable pageable);

    // 저자로 검색 (부분 일치)
    Page<Book> findByAuthorContaining(String author, Pageable pageable);

    // 출판사로 검색 (부분 일치)
    Page<Book> findByPublisherContaining(String publisher, Pageable pageable);

    // === 복합 조건 검색 ===

    // 카테고리 + 키워드 검색
    @Query("SELECT b FROM Book b WHERE b.category = :category AND " +
            "(b.bookName LIKE %:keyword% OR b.author LIKE %:keyword% OR b.publisher LIKE %:keyword%)")
    Page<Book> searchByCategoryAndKeyword(@Param("category") CategoryBottom category,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    // 카테고리 + 판매상태 검색
    Page<Book> findByCategoryAndSalesStatus(CategoryBottom category,
                                            Book.SalesStatus salesStatus,
                                            Pageable pageable);

    // 가격 범위 검색
    Page<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // === 베스트셀러 관련 ===

    // 판매지수 기준 상위 도서 조회
    @Query("SELECT b FROM Book b WHERE b.salesStatus = '판매중' ORDER BY b.salesIndex DESC")
    List<Book> findTopBySalesIndex(Pageable pageable);

    // 평점 기준 상위 도서 조회
    @Query("SELECT b FROM Book b WHERE b.salesStatus = '판매중' AND b.rating IS NOT NULL ORDER BY b.rating DESC")
    List<Book> findTopByRating(Pageable pageable);

    // === 등록일 기준 조회 (관리자 기능에서 사용) ===

    // 등록일 범위로 조회
    Page<Book> findByRegistrationDateBetween(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             Pageable pageable);

    // 최근 등록된 도서 조회
    @Query("SELECT b FROM Book b ORDER BY b.registrationDate DESC")
    List<Book> findRecentBooks(Pageable pageable);

    // === 재고 관련 조회 ===

    // 재고가 있는 도서만 조회
    @Query("SELECT b FROM Book b JOIN b.inventory i WHERE i.stockQuantity > 0")
    Page<Book> findBooksWithStock(Pageable pageable);

    // 재고 부족 도서 조회 (최소 재고 수준 이하)
    @Query("SELECT b FROM Book b JOIN b.inventory i WHERE i.stockQuantity <= i.minStockLevel")
    List<Book> findLowStockBooks();

    // === 카테고리 계층 구조 검색 ===

    // 대분류로 도서 검색
    @Query("SELECT b FROM Book b WHERE b.category.middleCategory.topCategory.categoryId = :topCategoryId")
    Page<Book> findByTopCategory(@Param("topCategoryId") Integer topCategoryId, Pageable pageable);

    // 중분류로 도서 검색
    @Query("SELECT b FROM Book b WHERE b.category.middleCategory.categoryId = :middleCategoryId")
    Page<Book> findByMiddleCategory(@Param("middleCategoryId") Integer middleCategoryId, Pageable pageable);

    // === 통계용 쿼리 ===

    // 카테고리별 도서 수 조회
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.categoryId = :categoryId")
    Long countByCategory(@Param("categoryId") Integer categoryId);

    // 판매상태별 도서 수 조회
    Long countBySalesStatus(Book.SalesStatus salesStatus);

    // 특정 저자의 도서 수 조회
    Long countByAuthor(String author);

    // 특정 출판사의 도서 수 조회
    Long countByPublisher(String publisher);
}
