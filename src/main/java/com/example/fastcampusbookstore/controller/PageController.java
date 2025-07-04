package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.service.CategoryService;
import com.example.fastcampusbookstore.service.BookService;
import com.example.fastcampusbookstore.service.PopularKeywordService;
import com.example.fastcampusbookstore.dto.response.CategoryTreeResponse;
import com.example.fastcampusbookstore.dto.request.BookSearchRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.response.BookListResponse;
import com.example.fastcampusbookstore.dto.response.BookDetailResponse;
import com.example.fastcampusbookstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final CategoryService categoryService;
    private final BookService bookService;
    private final PopularKeywordService popularKeywordService;
    private final ReviewService reviewService;

    // 기본 페이지들 - 단순 라우팅만
    @GetMapping("/cart")
    public String cart(Model model) {
        addCategoryData(model); // 네비게이션용 카테고리 추가
        return "cart";
    }

    // BookController에서 /product/{bookId} 를 처리하므로 여기서는 제거
    // @GetMapping("/product/{bookId}") - 이 부분 삭제!

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        addCategoryData(model); // 네비게이션용 카테고리 추가
        return "mypage";
    }

    // 상품 상세 페이지 - BookController의 중복을 피해 여기에 추가
    @GetMapping("/product/{bookId}")
    public String productDetail(@PathVariable Integer bookId, Model model) {
        addCategoryData(model); // 네비게이션용 카테고리 추가
        model.addAttribute("bookId", bookId);

        // 상품 상세 정보
        BookDetailResponse book = null;
        try {
            book = bookService.getBookDetail(bookId);
        } catch (Exception e) {
            log.warn("상품 상세 정보 조회 실패: {}", bookId, e);
        }
        model.addAttribute("book", book);

        // 별점(★, ☆) 문자열 생성 (book.rating)
        String bookRatingStars = "";
        if (book != null && book.getRating() != null) {
            int fullStars = book.getRating().intValue();
            int emptyStars = 5 - fullStars;
            bookRatingStars = "★".repeat(fullStars) + "☆".repeat(emptyStars) + " (" + book.getRating() + "/5)";
        }
        model.addAttribute("bookRatingStars", bookRatingStars);

        // 상품 설명 개행 처리
        String bookDescriptionHtml = (book != null && book.getDescription() != null) ? book.getDescription().replace("\n", "<br>") : "";
        model.addAttribute("bookDescriptionHtml", bookDescriptionHtml);

        // 리뷰 목록 (최신순 1페이지, 5개)
        Pageable pageable = PageRequest.of(0, 5);
        var reviewPage = reviewService.getBookReviews(bookId, pageable);
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("reviewPage", reviewPage);

        // 리뷰 요약 (평균 평점, 총 리뷰 수)
        Double averageRating = null;
        long totalReviews = 0;
        try {
            averageRating = reviewService.getAverageRating(bookId);
            totalReviews = reviewService.getReviewCount(bookId);
        } catch (Exception e) {
            log.warn("리뷰 요약 정보 조회 실패: {}", bookId, e);
        }
        model.addAttribute("reviewSummary", Map.of(
            "averageRating", averageRating != null ? String.format("%.1f", averageRating) : "0.0",
            "totalReviews", totalReviews
        ));

        // 리뷰 별점(★, ☆) 문자열 생성 (reviewSummary.averageRating)
        String reviewSummaryStars = "";
        if (averageRating != null) {
            int fullStars = averageRating.intValue();
            int emptyStars = 5 - fullStars;
            reviewSummaryStars = "★".repeat(fullStars) + "☆".repeat(emptyStars);
        }
        model.addAttribute("reviewSummaryStars", reviewSummaryStars);

        // 페이지네이션 범위 계산 (최대 5개 버튼)
        int currentPage = reviewPage.getNumber();
        int totalPages = reviewPage.getTotalPages();
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        java.util.List<Integer> pageNumbers = new java.util.ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);

        return "product";
    }

    // 회원정보 수정 페이지
    @GetMapping("/member/edit")
    public String memberEdit(Model model, HttpSession session) {

        // 로그인 확인
        String memberId = (String) session.getAttribute("memberId");
        if (memberId == null) {
            return "redirect:/login";
        }

        addCategoryData(model); // 네비게이션용 카테고리 추가
        return "member-edit";
    }

    // 카테고리별 상품 목록 페이지
    @GetMapping("/category/{categoryId}")
    public String categoryBooks(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "registration_date") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            Model model) {

        log.info("카테고리 페이지 요청 - categoryId: {}, page: {}, keyword: {}", categoryId, page, keyword);

        try {
            // 전체 카테고리 트리 조회 (네비게이션용)
            CategoryTreeResponse categoryTree = categoryService.getAllCategoriesTree();
            model.addAttribute("categories", categoryTree.getCategories());

            // 해당 카테고리의 상품 조회
            BookSearchRequest request = new BookSearchRequest(page, size);
            request.setCategoryId(categoryId);
            request.setSort(sort);
            request.setDirection(direction);
            if (keyword != null && !keyword.trim().isEmpty()) {
                request.setKeyword(keyword);
            }

            PageResponse<BookListResponse> booksResponse = bookService.getBooksByCategory(request);

            model.addAttribute("books", booksResponse.getContent());
            model.addAttribute("currentPage", booksResponse.getCurrentPage());
            model.addAttribute("totalPages", booksResponse.getTotalPages());
            model.addAttribute("totalElements", booksResponse.getTotalElements());
            model.addAttribute("size", booksResponse.getSize());
            model.addAttribute("hasNext", booksResponse.isHasNext());
            model.addAttribute("hasPrevious", booksResponse.isHasPrevious());

            // 현재 카테고리 정보
            model.addAttribute("currentCategoryId", categoryId);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentKeyword", keyword);

            log.info("카테고리 페이지 데이터 로드 완료 - 총 {}개 상품", booksResponse.getTotalElements());

            return "category";

        } catch (Exception e) {
            log.error("카테고리 페이지 로드 실패 - categoryId: {}", categoryId, e);
            model.addAttribute("error", "카테고리 정보를 불러오는 중 오류가 발생했습니다.");
            return "error";
        }
    }

    // 검색 결과 페이지
    @GetMapping("/search")
    public String searchResults(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "registration_date") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {

        log.info("검색 페이지 요청 - keyword: {}, page: {}", keyword, page);

        try {
            // 인기 검색어 카운트 증가
            if (keyword != null && !keyword.trim().isEmpty()) {
                popularKeywordService.increaseCount(keyword);
            }
            // 카테고리 네비게이션 추가
            addCategoryData(model);

            // 검색 수행
            BookSearchRequest request = new BookSearchRequest(page, size);
            request.setKeyword(keyword);
            request.setSort(sort);
            request.setDirection(direction);

            PageResponse<BookListResponse> searchResults = bookService.searchBooks(request);

            model.addAttribute("books", searchResults.getContent());
            model.addAttribute("currentPage", searchResults.getCurrentPage());
            model.addAttribute("totalPages", searchResults.getTotalPages());
            model.addAttribute("totalElements", searchResults.getTotalElements());
            model.addAttribute("size", searchResults.getSize());
            model.addAttribute("hasNext", searchResults.isHasNext());
            model.addAttribute("hasPrevious", searchResults.isHasPrevious());

            // 검색 정보
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);

            log.info("검색 완료 - 키워드: {}, 결과: {}개", keyword, searchResults.getTotalElements());

            return "search-results";

        } catch (Exception e) {
            log.error("검색 실패 - keyword: {}", keyword, e);
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");
            return "error";
        }
    }

    // 공통 메서드: 카테고리 데이터 추가
    private void addCategoryData(Model model) {
        try {
            CategoryTreeResponse categoryTree = categoryService.getAllCategoriesTree();
            model.addAttribute("categories", categoryTree.getCategories());
        } catch (Exception e) {
            log.warn("카테고리 데이터 로드 실패", e);
            // 카테고리 로드 실패해도 페이지는 표시
        }
    }
}