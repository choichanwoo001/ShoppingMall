package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.BookSearchRequest;
import com.example.fastcampusbookstore.dto.request.BookRegisterRequest;
import com.example.fastcampusbookstore.dto.request.BookUpdateRequest;
import com.example.fastcampusbookstore.dto.request.InventorySearchRequest;
import com.example.fastcampusbookstore.dto.response.BookDetailResponse;
import com.example.fastcampusbookstore.dto.response.BookListResponse;
import com.example.fastcampusbookstore.dto.response.BestsellerResponse;
import com.example.fastcampusbookstore.dto.response.InventoryResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Bestseller;
import com.example.fastcampusbookstore.entity.CategoryBottom;
import com.example.fastcampusbookstore.entity.CategoryTop;
import com.example.fastcampusbookstore.entity.CategoryMiddle;
import com.example.fastcampusbookstore.entity.Inventory;
import com.example.fastcampusbookstore.repository.BookRepository;
import com.example.fastcampusbookstore.repository.BestsellerRepository;
import com.example.fastcampusbookstore.repository.CategoryBottomRepository;
import com.example.fastcampusbookstore.repository.CategoryTopRepository;
import com.example.fastcampusbookstore.repository.CategoryMiddleRepository;
import com.example.fastcampusbookstore.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// BookService, CategoryService, PopularKeywordService의 모든 메서드와 의존성 주입을 이 파일로 합친다.
// 클래스명: BookCategoryService
// @Service, @Transactional 등은 그대로 유지
// CategoryService, PopularKeywordService의 메서드와 의존성 주입을 이 클래스에 추가
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCategoryService {

    private final BookRepository bookRepository;
    private final BestsellerRepository bestsellerRepository;
    private final CategoryBottomRepository categoryBottomRepository;
    private final CategoryTopRepository categoryTopRepository;
    private final CategoryMiddleRepository categoryMiddleRepository;
    private final InventoryRepository inventoryRepository;

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

    // 3-1. 모든 베스트셀러 목록 조회 (월 구분 없이)
    public List<BestsellerResponse> getAllBestsellers() {

        List<Bestseller> bestsellers = bestsellerRepository.findAllByOrderByRanking();

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

    // 5. 판매지수 기준 상위 도서 조회 (추가된 메서드)
    public List<Book> getTopBySalesIndex(Pageable pageable) {
        return bookRepository.findTopBySalesIndex(pageable);
    }

    // === 관리자 기능 ===

    // 관리자용 상품 목록 조회
    public PageResponse<BookListResponse> getBookListForAdmin(BookSearchRequest request, PageRequest pageRequest) {
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(request.getBookName())) {
                predicates.add(cb.like(root.get("bookName"), "%" + request.getBookName() + "%"));
            }
            if (StringUtils.hasText(request.getPublisher())) {
                predicates.add(cb.like(root.get("publisher"), "%" + request.getPublisher() + "%"));
            }
            if (StringUtils.hasText(request.getAuthor())) {
                predicates.add(cb.like(root.get("author"), "%" + request.getAuthor() + "%"));
            }
            if (StringUtils.hasText(request.getSalesStatus()) && !request.getSalesStatus().equals("")) {
                predicates.add(cb.equal(root.get("salesStatus"), Book.SalesStatus.valueOf(request.getSalesStatus())));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(cb.between(root.get("registerDate"), 
                    request.getStartDate().atStartOfDay(), 
                    request.getEndDate().atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 정렬 조건 적용
        Sort sort = Sort.by(Sort.Direction.DESC, "registerDate"); // 기본값
        if (StringUtils.hasText(request.getSortBy())) {
            String sortField = switch (request.getSortBy()) {
                case "bookName" -> "bookName";
                case "price" -> "price";
                case "registerDate" -> "registerDate";
                default -> "registerDate";
            };
            Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        
        List<BookListResponse> content = bookPage.getContent().stream()
            .map(BookListResponse::from)
            .collect(Collectors.toList());
            
        return PageResponse.of(content, bookPage);
    }

    // 상품 등록
    @Transactional
    public void registerBook(BookRegisterRequest request) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setBookName(request.getBookName());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setDescription(request.getDescription());
        book.setPrice(BigDecimal.valueOf(request.getPrice()));
        book.setBookImage(request.getBookImage());
        book.setPreviewPdf(request.getPreviewPdf());
        book.setBookSize(request.getBookSize());
        book.setRating(request.getRating() != null ? BigDecimal.valueOf(request.getRating()) : null);
        book.setSalesIndex(request.getSalesIndex());
        book.setSalesStatus(Book.SalesStatus.valueOf(request.getSalesStatus()));
        book.setRegisterDate(LocalDateTime.now());
        
        // 카테고리 설정
        if (StringUtils.hasText(request.getCategoryTop())) {
            CategoryTop categoryTop = categoryTopRepository.findByCategoryName(request.getCategoryTop())
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
            book.setCategoryTop(categoryTop);
        }
        
        if (StringUtils.hasText(request.getCategoryMiddle())) {
            CategoryMiddle categoryMiddle = categoryMiddleRepository.findByCategoryName(request.getCategoryMiddle())
                .orElseThrow(() -> new RuntimeException("중위 카테고리를 찾을 수 없습니다."));
            book.setCategoryMiddle(categoryMiddle);
        }
        
        if (StringUtils.hasText(request.getCategoryBottom())) {
            CategoryBottom categoryBottom = categoryBottomRepository.findByCategoryName(request.getCategoryBottom())
                .orElseThrow(() -> new RuntimeException("하위 카테고리를 찾을 수 없습니다."));
            book.setCategoryBottom(categoryBottom);
        }
        
        bookRepository.save(book);
        
        // 재고 정보 생성
        Inventory inventory = new Inventory();
        inventory.setBook(book);
        inventory.setStockQuantity(request.getStockQuantity());
        inventoryRepository.save(inventory);
    }

    // 재고 목록 조회
    public PageResponse<InventoryResponse> getInventoryList(InventorySearchRequest request, PageRequest pageRequest) {
        Specification<Inventory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(request.getBookName())) {
                predicates.add(cb.like(root.get("book").get("bookName"), "%" + request.getBookName() + "%"));
            }
            if (StringUtils.hasText(request.getPublisher())) {
                predicates.add(cb.like(root.get("book").get("publisher"), "%" + request.getPublisher() + "%"));
            }
            if (StringUtils.hasText(request.getAuthor())) {
                predicates.add(cb.like(root.get("book").get("author"), "%" + request.getAuthor() + "%"));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(cb.between(root.get("book").get("registerDate"), 
                    request.getStartDate().atStartOfDay(), 
                    request.getEndDate().atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 정렬 조건 적용 - Inventory 엔티티 기준으로 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "book.registerDate"); // 기본값
        if (StringUtils.hasText(request.getSortBy())) {
            String sortField = switch (request.getSortBy()) {
                case "bookName" -> "book.bookName";
                case "price" -> "book.price";
                case "registerDate" -> "book.registerDate";
                default -> "book.registerDate";
            };
            Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
        Page<Inventory> inventoryPage = inventoryRepository.findAll(spec, pageable);
        
        List<InventoryResponse> content = inventoryPage.getContent().stream()
            .map(this::convertToInventoryResponse)
            .collect(Collectors.toList());
            
        return PageResponse.of(content, inventoryPage);
    }

    // 재고 수정
    @Transactional
    public void updateInventory(Long bookId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBookBookId(bookId.intValue())
            .orElseThrow(() -> new RuntimeException("재고 정보를 찾을 수 없습니다."));
        inventory.setStockQuantity(quantity);
    }

    // 재고 응답 변환
    private InventoryResponse convertToInventoryResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setBookId(Long.valueOf(inventory.getBook().getBookId()));
        response.setIsbn(inventory.getBook().getIsbn());
        response.setBookName(inventory.getBook().getBookName());
        response.setAuthor(inventory.getBook().getAuthor());
        response.setPublisher(inventory.getBook().getPublisher());
        response.setPrice(inventory.getBook().getPrice().intValue());
        response.setStockQuantity(inventory.getStockQuantity());
        response.setSalesStatus(inventory.getBook().getSalesStatus().toString());
        response.setRegisterDate(inventory.getBook().getRegisterDate());
        response.setBookImage(inventory.getBook().getBookImage());
        return response;
    }

    // CategoryService에서 제공하던 메서드 추가
    public com.example.fastcampusbookstore.dto.response.CategoryTreeResponse getAllCategoriesTree() {
        java.util.List<com.example.fastcampusbookstore.entity.CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();
        java.util.List<com.example.fastcampusbookstore.dto.response.CategoryResponse> categoryResponses = topCategories.stream()
                .map(com.example.fastcampusbookstore.dto.response.CategoryResponse::fromTop)
                .collect(java.util.stream.Collectors.toList());
        return new com.example.fastcampusbookstore.dto.response.CategoryTreeResponse(categoryResponses);
    }
    public java.util.List<com.example.fastcampusbookstore.dto.response.CategoryResponse> getTopCategories() {
        java.util.List<com.example.fastcampusbookstore.entity.CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();
        return topCategories.stream()
                .map(com.example.fastcampusbookstore.dto.response.CategoryResponse::fromTop)
                .collect(java.util.stream.Collectors.toList());
    }
    public java.util.List<com.example.fastcampusbookstore.dto.response.CategoryResponse> getMiddleCategories(Integer topCategoryId) {
        com.example.fastcampusbookstore.entity.CategoryTop topCategory = categoryTopRepository.findById(topCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대분류 카테고리입니다"));
        java.util.List<com.example.fastcampusbookstore.entity.CategoryMiddle> middleCategories = categoryMiddleRepository
                .findByTopCategoryAndIsActiveTrueOrderBySortOrder(topCategory);
        return middleCategories.stream()
                .map(com.example.fastcampusbookstore.dto.response.CategoryResponse::fromMiddle)
                .collect(java.util.stream.Collectors.toList());
    }
    public java.util.List<com.example.fastcampusbookstore.dto.response.CategoryResponse> getBottomCategories(Integer middleCategoryId) {
        com.example.fastcampusbookstore.entity.CategoryMiddle middleCategory = categoryMiddleRepository.findById(middleCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 중분류 카테고리입니다"));
        java.util.List<com.example.fastcampusbookstore.entity.CategoryBottom> bottomCategories = categoryBottomRepository
                .findByMiddleCategoryAndIsActiveTrueOrderBySortOrder(middleCategory);
        return bottomCategories.stream()
                .map(com.example.fastcampusbookstore.dto.response.CategoryResponse::fromBottom)
                .collect(java.util.stream.Collectors.toList());
    }
    public java.util.List<com.example.fastcampusbookstore.dto.response.CategoryResponse> getAllCategories() {
        java.util.List<com.example.fastcampusbookstore.entity.CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();
        return topCategories.stream()
                .map(com.example.fastcampusbookstore.dto.response.CategoryResponse::fromTop)
                .collect(java.util.stream.Collectors.toList());
    }

    // 인기 검색어 관련 메서드 추가
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicInteger> keywordCount = new java.util.concurrent.ConcurrentHashMap<>();
    public void increaseCount(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        keywordCount.computeIfAbsent(keyword.trim(), k -> new java.util.concurrent.atomic.AtomicInteger(0)).incrementAndGet();
    }
    public java.util.List<String> getTopKeywords(int n) {
        return keywordCount.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
    public java.util.Map<String, Integer> getAllKeywordCounts() {
        return keywordCount.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, e -> e.getValue().get()));
    }

    public long countBooks() {
        return bookRepository.count();
    }
    public long countOutOfStockBooks() {
        return inventoryRepository.countByStockQuantity(0);
    }

    // === Private 헬퍼 메서드들 ===

    private Pageable createPageable(BookSearchRequest request) {
        Sort sort = createSort(request.getSort(), request.getDirection());
        return org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), sort);
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