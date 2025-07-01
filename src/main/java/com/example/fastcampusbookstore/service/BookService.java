package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.BookSearchRequest;
import com.example.fastcampusbookstore.dto.response.BookDetailResponse;
import com.example.fastcampusbookstore.dto.response.BookListResponse;
import com.example.fastcampusbookstore.dto.response.BestsellerResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Bestseller;
import com.example.fastcampusbookstore.entity.CategoryBottom;
import com.example.fastcampusbookstore.repository.BookRepository;
import com.example.fastcampusbookstore.repository.BestsellerRepository;
import com.example.fastcampusbookstore.repository.CategoryBottomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BestsellerRepository bestsellerRepository;
    private final CategoryBottomRepository categoryBottomRepository;

    // 1. 상품 검색 (통합 검색)
    public PageResponse<BookListResponse> searchBooks(BookSearchRequest request) {

        Pageable pageable = createPageable(request);
        Page<Book> books;

        if (StringUtils.hasText(request.getKeyword())) {
            // 키워드 검색 (책이름, 저자, 출판사)
            books = bookRepository.searchByKeyword(request.getKeyword(), pageable);
        } else {
            // 전체 조회
            books = bookRepository.findAll(pageable);
        }

        List<BookListResponse> bookResponses = books.getContent().stream()
                .map(BookListResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(bookResponses, books);
    }

    // 2. 상품 상세 조회
    public BookDetailResponse getBookDetail(Integer bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

        return BookDetailResponse.from(book);
    }

    // 3. 베스트셀러 목록 조회
    public List<BestsellerResponse> getBestsellers(String targetMonth) {

        List<Bestseller> bestsellers = bestsellerRepository.findByTargetMonthOrderByRanking(targetMonth);

        return bestsellers.stream()
                .map(BestsellerResponse::from)
                .collect(Collectors.toList());
    }

    // 4. 카테고리별 상품 조회
    public PageResponse<BookListResponse> getBooksByCategory(BookSearchRequest request) {

        CategoryBottom category = categoryBottomRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다"));

        Pageable pageable = createPageable(request);
        Page<Book> books;

        if (StringUtils.hasText(request.getKeyword())) {
            // 카테고리 + 키워드 검색
            books = bookRepository.searchByCategoryAndKeyword(category, request.getKeyword(), pageable);
        } else {
            // 카테고리만 검색
            books = bookRepository.findByCategory(category, pageable);
        }

        List<BookListResponse> bookResponses = books.getContent().stream()
                .map(BookListResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(bookResponses, books);
    }

    // === Private 헬퍼 메서드들 ===

    private Pageable createPageable(BookSearchRequest request) {
        Sort sort = createSort(request.getSort(), request.getDirection());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Sort createSort(String sortField, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // 정렬 조건 매핑 (요구사항: 상품의 이름, 가격, 등록일)
        String mappedField = switch (sortField) {
            case "name" -> "bookName";
            case "price" -> "price";
            case "registration_date" -> "registrationDate";
            default -> "registrationDate";
        };

        return Sort.by(sortDirection, mappedField);
    }
}