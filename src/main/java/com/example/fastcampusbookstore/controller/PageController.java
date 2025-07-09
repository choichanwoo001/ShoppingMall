package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.service.*;
import com.example.fastcampusbookstore.dto.response.*;
import com.example.fastcampusbookstore.dto.request.BookSearchRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.service.BookService;
import com.example.fastcampusbookstore.service.ReviewService;
import com.example.fastcampusbookstore.service.RecentViewService;
import com.example.fastcampusbookstore.service.CategoryService;
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
import org.springframework.data.domain.Sort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final RecentViewService recentViewService;
    private final PopularKeywordService popularKeywordService;

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        HttpSession session,
                        Model model) {

        log.info("메인 페이지 요청 - 검색어: {}", search);

        try {
            // 1. 카테고리 트리 조회 (네비게이션용)
            List<CategoryResponse> categories = categoryService.getAllCategoriesTree().getCategories();

            // 2. 이달의 베스트셀러 조회 (상위 12개)
            String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<BestsellerResponse> bestsellers = bookService.getAllBestsellers();

            // 베스트셀러가 12개보다 적으면 12개로 제한
            if (bestsellers.size() > 12) {
                bestsellers = bestsellers.subList(0, 12);
            }

            // 3. 인기 도서 (판매지수 기준 상위 5개)
            List<Book> popularBooks = getPopularBooks();

            // 4. 최근 본 상품 (로그인한 경우만, 상위 3개)
            List<RecentViewResponse> recentViews = null;
            String memberId = (String) session.getAttribute("memberId");

            if (memberId != null) {
                recentViews = recentViewService.getRecentViews(memberId, 3);
            }

            // 5. 검색 기능 (검색어가 있는 경우)
            if (search != null && !search.trim().isEmpty()) {
                // 검색 결과 페이지로 리다이렉트 (한글 인코딩)
                String encoded = URLEncoder.encode(search, StandardCharsets.UTF_8);
                return "redirect:/search?keyword=" + encoded;
            }

            // 6. 모델에 데이터 추가
            model.addAttribute("categories", categories);
            model.addAttribute("bestsellers", bestsellers);
            model.addAttribute("popularBooks", popularBooks);
            model.addAttribute("recentViews", recentViews);
            model.addAttribute("currentMonth", currentMonth);
            
            // 레이아웃 관련 속성 추가
            model.addAttribute("title", "온라인 서점");
            model.addAttribute("cssFiles", List.of("main.css"));
            model.addAttribute("jsFiles", List.of("main.js"));

            log.info("메인 페이지 데이터 로드 완료 - 베스트셀러: {}개, 인기도서: {}개, 최근본상품: {}개",
                    bestsellers.size(),
                    popularBooks.size(),
                    recentViews != null ? recentViews.size() : 0);

            return "index";

        } catch (Exception e) {
            log.error("메인 페이지 로드 실패", e);

            // 에러 발생 시 빈 데이터로 페이지 표시
            model.addAttribute("categories", List.of());
            model.addAttribute("bestsellers", List.of());
            model.addAttribute("popularBooks", List.of());
            model.addAttribute("recentViews", List.of());
            model.addAttribute("errorMessage", "일부 데이터를 불러올 수 없습니다.");

            return "index";
        }
    }

    // index 별칭
    @GetMapping("/index")
    public String indexAlias(@RequestParam(required = false) String search,
                             HttpSession session,
                             Model model) {
        return index(search, session, model);
    }

    // 기본 페이지들 - 단순 라우팅만
    @GetMapping("/cart")
    public String cart(Model model) {
        addCategoryData(model); // 네비게이션용 카테고리 추가
        
        // 레이아웃 관련 속성 추가
        model.addAttribute("title", "장바구니");
        model.addAttribute("cssFiles", java.util.List.of("cart.css"));
        model.addAttribute("jsFiles", java.util.List.of("cart.js"));
        
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
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/mypage")
    public String mypage(Model model, HttpSession session) {
        addCategoryData(model); // 네비게이션용 카테고리 추가
        Object loginMember = session.getAttribute("loginMember");
        if (loginMember != null) {
            model.addAttribute("member", loginMember);
        }
        
        // 레이아웃 관련 속성 추가
        model.addAttribute("title", "마이페이지");
        model.addAttribute("cssFiles", java.util.List.of("mypage.css"));
        model.addAttribute("jsFiles", java.util.List.of("mypage.js"));
        
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

        // // 리뷰 목록 (최신순 1페이지, 5개)
        // Pageable pageable = PageRequest.of(0, 5);
        // var reviewPage = reviewService.getBookReviews(bookId, pageable);
        
            // 리뷰 목록 (최신순 1페이지, 5개)
        Pageable pageable = PageRequest.of(0, 5);
        var reviewPage = reviewService.getBookReviews(bookId, pageable);
        
        // 페이지네이션 정보 계산 추가
        int startItem = reviewPage.getNumber() * reviewPage.getSize() + 1;
        int endItem = Math.min((reviewPage.getNumber() + 1) * reviewPage.getSize(), 
                            (int) reviewPage.getTotalElements());
        
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
    
        // 각 리뷰의 별점을 별표 문자열로 변환
        var reviews = reviewPage.getContent().stream()
            .map(review -> {
                // 별점을 별표로 변환
                String ratingStars = "";
                if (review.getRating() != null) {
                    int fullStars = review.getRating();
                    int emptyStars = 5 - fullStars;
                    ratingStars = "★".repeat(fullStars) + "☆".repeat(emptyStars);
                }
                
                // ReviewResponse에 ratingStars 필드 추가 (임시로 Map 사용)
                java.util.Map<String, Object> reviewWithStars = new java.util.HashMap<>();
                reviewWithStars.put("reviewId", review.getReviewId());
                reviewWithStars.put("rating", review.getRating());
                reviewWithStars.put("reviewTitle", review.getReviewTitle());
                reviewWithStars.put("reviewContent", review.getReviewContent());
                reviewWithStars.put("isRecommended", review.getIsRecommended());
                reviewWithStars.put("createdAt", review.getCreatedAt());
                reviewWithStars.put("updatedAt", review.getUpdatedAt());
                reviewWithStars.put("reviewer", review.getReviewer());
                reviewWithStars.put("book", review.getBook());
                reviewWithStars.put("ratingStars", ratingStars);
                
                return reviewWithStars;
            })
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("reviews", reviews);
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

        // 레이아웃 관련 속성 추가
        model.addAttribute("title", book != null ? book.getBookName() : "상품 상세");
        model.addAttribute("cssFiles", java.util.List.of("product.css"));
        model.addAttribute("jsFiles", java.util.List.of("product.js"));

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
        
        // 레이아웃 관련 속성 추가
        model.addAttribute("title", "회원정보 수정");
        model.addAttribute("cssFiles", java.util.List.of("member-edit.css"));
        model.addAttribute("jsFiles", java.util.List.of("member-edit.js"));
        
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
            model.addAttribute("currentCategoryId", String.valueOf(categoryId));
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentKeyword", keyword);

            log.info("카테고리 페이지 데이터 로드 완료 - 총 {}개 상품", booksResponse.getTotalElements());

            // 레이아웃 관련 속성 추가
            model.addAttribute("title", "카테고리별 도서");
            model.addAttribute("cssFiles", java.util.List.of("category.css"));
            model.addAttribute("jsFiles", java.util.List.of("category.js"));

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

            // 레이아웃 관련 속성 추가
            model.addAttribute("title", "\"" + keyword + "\" 검색결과");
            model.addAttribute("cssFiles", java.util.List.of("category.css")); // 검색결과는 카테고리와 같은 스타일 사용
            model.addAttribute("jsFiles", java.util.List.of("search.js"));

            return "search-results";

        } catch (Exception e) {
            log.error("검색 실패 - keyword: {}", keyword, e);
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");
            return "error";
        }
    }

    @GetMapping("/order")
    public String orderPage(Model model, HttpSession session) {
        addCategoryData(model);
        // 필요시 세션에서 회원정보 등 추가
        
        // 레이아웃 관련 속성 추가
        model.addAttribute("title", "주문서 작성");
        model.addAttribute("cssFiles", java.util.List.of("cart.css", "order.css"));
        model.addAttribute("jsFiles", java.util.List.of("order.js"));
        
        return "order";
    }

    @GetMapping("/order/success")
    public String orderSuccess(Model model) {
        addCategoryData(model);
        model.addAttribute("title", "주문 완료");
        model.addAttribute("cssFiles", java.util.List.of("order.css"));
        model.addAttribute("jsFiles", java.util.List.of());
        return "order-success";
    }

    @GetMapping("/order/fail")
    public String orderFail(Model model) {
        addCategoryData(model);
        model.addAttribute("title", "결제 실패");
        model.addAttribute("cssFiles", java.util.List.of("order.css"));
        model.addAttribute("jsFiles", java.util.List.of());
        return "order-fail";
    }

    @GetMapping("/order/cancel")
    public String orderCancel(Model model) {
        addCategoryData(model);
        model.addAttribute("title", "결제 취소");
        model.addAttribute("cssFiles", java.util.List.of("order.css"));
        model.addAttribute("jsFiles", java.util.List.of());
        return "order-cancel";
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
    /**
     * 인기 도서 조회 (판매지수 기준)
     */
    private List<Book> getPopularBooks() {
        try {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "salesIndex"));
            return bookService.getTopBySalesIndex(pageable);
        } catch (Exception e) {
            log.warn("인기 도서 조회 실패", e);
            return List.of();
        }
    }
}