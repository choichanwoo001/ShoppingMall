package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.*;
import com.example.fastcampusbookstore.dto.response.*;
import com.example.fastcampusbookstore.entity.*;
import com.example.fastcampusbookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.fastcampusbookstore.repository.AdminRepository;
import java.util.Optional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryTopRepository categoryTopRepository;
    private final CategoryMiddleRepository categoryMiddleRepository;
    private final CategoryBottomRepository categoryBottomRepository;
    private final AdminRepository adminRepository;

    // 상품 목록 조회 (관리자용)
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
        book.setPrice(java.math.BigDecimal.valueOf(request.getPrice()));
        book.setBookImage(request.getBookImage());
        book.setPreviewPdf(request.getPreviewPdf());
        book.setBookSize(request.getBookSize());
        book.setRating(request.getRating() != null ? java.math.BigDecimal.valueOf(request.getRating()) : null);
        book.setSalesIndex(request.getSalesIndex());
        book.setSalesStatus(Book.SalesStatus.valueOf(request.getSalesStatus()));
        book.setRegisterDate(LocalDateTime.now());
        
        // 카테고리 설정
        if (request.getCategoryTop() != null) {
            CategoryTop categoryTop = categoryTopRepository.findByCategoryName(request.getCategoryTop())
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
            book.setCategoryTop(categoryTop);
        }
        
        if (request.getCategoryMiddle() != null) {
            CategoryMiddle categoryMiddle = categoryMiddleRepository.findByCategoryName(request.getCategoryMiddle())
                .orElseThrow(() -> new RuntimeException("중위 카테고리를 찾을 수 없습니다."));
            book.setCategoryMiddle(categoryMiddle);
        }
        
        if (request.getCategoryBottom() != null) {
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

    // 상품 수정
    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest request) {
        Book book = bookRepository.findById(bookId.intValue())
            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        book.setIsbn(request.getIsbn());
        book.setBookName(request.getBookName());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setDescription(request.getDescription());
        book.setPrice(java.math.BigDecimal.valueOf(request.getPrice()));
        book.setBookImage(request.getBookImage());
        book.setPreviewPdf(request.getPreviewPdf());
        book.setBookSize(request.getBookSize());
        book.setRating(request.getRating() != null ? java.math.BigDecimal.valueOf(request.getRating()) : null);
        book.setSalesIndex(request.getSalesIndex());
        book.setSalesStatus(Book.SalesStatus.valueOf(request.getSalesStatus()));
        
        // 카테고리 설정
        if (request.getCategoryTop() != null) {
            CategoryTop categoryTop = categoryTopRepository.findByCategoryName(request.getCategoryTop())
                .orElseThrow(() -> new RuntimeException("상위 카테고리를 찾을 수 없습니다."));
            book.setCategoryTop(categoryTop);
        }
        
        if (request.getCategoryMiddle() != null) {
            CategoryMiddle categoryMiddle = categoryMiddleRepository.findByCategoryName(request.getCategoryMiddle())
                .orElseThrow(() -> new RuntimeException("중위 카테고리를 찾을 수 없습니다."));
            book.setCategoryMiddle(categoryMiddle);
        }
        
        if (request.getCategoryBottom() != null) {
            CategoryBottom categoryBottom = categoryBottomRepository.findByCategoryName(request.getCategoryBottom())
                .orElseThrow(() -> new RuntimeException("하위 카테고리를 찾을 수 없습니다."));
            book.setCategoryBottom(categoryBottom);
        }
    }

    // 주문 목록 조회 (관리자용)
    public PageResponse<OrderListResponse> getOrderListForAdmin(OrderSearchRequest request, PageRequest pageRequest) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getOrdererName() != null && !request.getOrdererName().isEmpty()) {
                predicates.add(cb.like(root.get("member").get("memberName"), "%" + request.getOrdererName() + "%"));
            }
            if (request.getBookName() != null && !request.getBookName().isEmpty()) {
                predicates.add(cb.like(root.join("orderDetails").get("book").get("bookName"), "%" + request.getBookName() + "%"));
            }
            if (request.getSalesStatus() != null && !request.getSalesStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("orderStatus"), request.getSalesStatus()));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(cb.between(root.get("orderDate"), 
                    request.getStartDate().atStartOfDay(), 
                    request.getEndDate().atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderListResponse> content = orderPage.getContent().stream()
            .map(this::convertToOrderListResponse)
            .collect(Collectors.toList());
        return PageResponse.of(content, orderPage);
    }

    // 회원 목록 조회 (관리자용)
    public PageResponse<MemberResponse> getMemberListForAdmin(MemberSearchRequest request, PageRequest pageRequest) {
        Specification<Member> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getMemberId() != null && !request.getMemberId().isEmpty()) {
                predicates.add(cb.like(root.get("memberId"), "%" + request.getMemberId() + "%"));
            }
            if (request.getMemberStatus() != null && !request.getMemberStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("memberStatus"), request.getMemberStatus()));
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                predicates.add(cb.like(root.get("email"), "%" + request.getEmail() + "%"));
            }
            if (request.getMemberGrade() != null && !request.getMemberGrade().isEmpty()) {
                predicates.add(cb.equal(root.get("memberGrade"), request.getMemberGrade()));
            }
            if (request.getMemberName() != null && !request.getMemberName().isEmpty()) {
                predicates.add(cb.like(root.get("memberName"), "%" + request.getMemberName() + "%"));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(cb.between(root.get("joinDate"), 
                    request.getStartDate().atStartOfDay(), 
                    request.getEndDate().atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        Page<Member> memberPage = memberRepository.findAll(spec, pageable);
        List<MemberResponse> content = memberPage.getContent().stream()
            .map(this::convertToMemberResponse)
            .collect(Collectors.toList());
        return PageResponse.of(content, memberPage);
    }

    // 재고 목록 조회
    public PageResponse<InventoryResponse> getInventoryList(InventorySearchRequest request, PageRequest pageRequest) {
        Specification<Inventory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getBookName() != null && !request.getBookName().isEmpty()) {
                predicates.add(cb.like(root.get("book").get("bookName"), "%" + request.getBookName() + "%"));
            }
            if (request.getPublisher() != null && !request.getPublisher().isEmpty()) {
                predicates.add(cb.like(root.get("book").get("publisher"), "%" + request.getPublisher() + "%"));
            }
            if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
                predicates.add(cb.like(root.get("book").get("author"), "%" + request.getAuthor() + "%"));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(cb.between(root.get("book").get("registerDate"), 
                    request.getStartDate().atStartOfDay(), 
                    request.getEndDate().atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
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

    public boolean existsAdmin(String adminId) {
        return adminRepository.findByAdminId(adminId).isPresent();
    }

    public Optional<Admin> findByAdminId(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    // 변환 메서드들
    private BookListResponse convertToBookListResponse(Book book) {
        BookListResponse response = new BookListResponse();
        response.setBookId(book.getBookId());
        response.setBookName(book.getBookName());
        response.setAuthor(book.getAuthor());
        response.setPublisher(book.getPublisher());
        response.setPrice(book.getPrice());
        response.setBookImage(book.getBookImage());
        response.setRating(book.getRating());
        response.setSalesStatus(book.getSalesStatus().toString());
        response.setRegisterDate(book.getRegisterDate());
        return response;
    }

    private OrderListResponse convertToOrderListResponse(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderDate(order.getOrderDate());
        response.setOrderStatus(order.getOrderStatus().toString());
        response.setTotalAmount(order.getTotalAmount());
        response.setMemberId(order.getMember().getMemberId());
        response.setMemberName(order.getMember().getMemberName());
        return response;
    }

    private MemberResponse convertToMemberResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setMemberId(member.getMemberId());
        response.setMemberName(member.getMemberName());
        response.setEmail(member.getEmail());
        response.setPhone(member.getPhone());
        response.setAddress(member.getAddress());
        response.setJoinDate(member.getJoinDate());
        response.setLastLoginDate(member.getLastLoginDate());
        response.setMemberGrade(member.getMemberGrade().toString());
        response.setMemberStatus(member.getMemberStatus().toString());
        return response;
    }

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
} 