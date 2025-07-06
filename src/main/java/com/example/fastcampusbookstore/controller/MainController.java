package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.response.BestsellerResponse;
import com.example.fastcampusbookstore.dto.response.CategoryResponse;
import com.example.fastcampusbookstore.dto.response.RecentViewResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.service.BookService;
import com.example.fastcampusbookstore.service.CategoryService;
import com.example.fastcampusbookstore.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final RecentViewService recentViewService;

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
                // 검색 결과 페이지로 리다이렉트
                return "redirect:/search?keyword=" + search;
            }

            // 6. 모델에 데이터 추가
            model.addAttribute("categories", categories);
            model.addAttribute("bestsellers", bestsellers);
            model.addAttribute("popularBooks", popularBooks);
            model.addAttribute("recentViews", recentViews);
            model.addAttribute("currentMonth", currentMonth);

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

    // 검색 메서드는 제거 - PageController에서 처리

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