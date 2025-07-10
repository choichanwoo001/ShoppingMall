package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.BookSearchRequest;
import com.example.fastcampusbookstore.dto.response.BookDetailResponse;
import com.example.fastcampusbookstore.dto.response.BookListResponse;
import com.example.fastcampusbookstore.dto.response.BestsellerResponse;
import com.example.fastcampusbookstore.service.BookCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

// BookController, CategoryController, PopularKeywordController의 모든 메서드와 의존성 주입을 이 파일로 합친다.
// 클래스명: BookCategoryController
// @RestController, @RequestMapping 등은 그대로 유지
// CategoryController, PopularKeywordController의 메서드와 의존성 주입을 이 클래스에 추가
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookController {

    private final BookCategoryService bookCategoryService;

    // 1. 상품 목록 조회 (검색 포함)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> getBooks(
            @Valid @ModelAttribute BookSearchRequest request) {

        log.info("상품 목록 조회 요청: keyword={}, page={}, size={}",
                request.getKeyword(), request.getPage(), request.getSize());

        try {
            PageResponse<BookListResponse> response = bookCategoryService.searchBooks(request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("상품 목록 조회 중 오류가 발생했습니다."));
        }
    }

    // 2. 상품 상세 조회
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookDetail(
            @PathVariable Integer bookId) {

        log.info("상품 상세 조회 요청: bookId={}", bookId);

        try {
            BookDetailResponse response = bookCategoryService.getBookDetail(bookId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("상품 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("상품 상세 조회 중 오류가 발생했습니다."));
        }
    }

    // 3. 베스트셀러 목록 조회
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<List<BestsellerResponse>>> getBestsellers(
            @RequestParam(defaultValue = "2024-12") String targetMonth) {

        log.info("베스트셀러 조회 요청: targetMonth={}", targetMonth);

        try {
            List<BestsellerResponse> response = bookCategoryService.getBestsellers(targetMonth);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("베스트셀러 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("베스트셀러 조회 중 오류가 발생했습니다."));
        }
    }

    // 4. 카테고리별 상품 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<BookListResponse>>> getBooksByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "registration_date") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("카테고리별 상품 조회 요청: categoryId={}, page={}, size={}",
                categoryId, page, size);

        try {
            BookSearchRequest request = new BookSearchRequest(page, size);
            request.setCategoryId(categoryId);
            request.setSort(sort);
            request.setDirection(direction);

            PageResponse<BookListResponse> response = bookCategoryService.getBooksByCategory(request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("카테고리별 상품 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("카테고리별 상품 조회 중 오류가 발생했습니다."));
        }
    }

    // 인기 검색어 카운트 증가
    @RequestMapping(value = "/popular-keywords/increase", method = RequestMethod.POST)
    public void increaseKeyword(@RequestParam String keyword) {
        bookCategoryService.increaseCount(keyword);
    }

    // 인기 검색어 Top N 반환
    @RequestMapping(value = "/popular-keywords/top", method = RequestMethod.GET)
    public List<String> getTopKeywords(@RequestParam(defaultValue = "5") int size) {
        return bookCategoryService.getTopKeywords(size);
    }

    // 전체 인기 검색어 카운트 반환
    @RequestMapping(value = "/popular-keywords/all", method = RequestMethod.GET)
    public Map<String, Integer> getAllKeywordCounts() {
        return bookCategoryService.getAllKeywordCounts();
    }
}