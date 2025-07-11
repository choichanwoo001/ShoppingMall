package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.common.PageRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.*;
import com.example.fastcampusbookstore.dto.response.*;
import com.example.fastcampusbookstore.service.BookCategoryService;
import com.example.fastcampusbookstore.service.OrderPaymentService;
import com.example.fastcampusbookstore.service.MemberService;
import com.example.fastcampusbookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookCategoryService bookCategoryService;
    private final OrderPaymentService orderPaymentService;
    private final MemberService memberService;
    private final AdminService adminService;

    // 관리자 메인 페이지
    @GetMapping("")
    public String adminMain(HttpSession session) {
        Object adminSession = session.getAttribute("adminId");
        if (adminSession == null || !adminService.existsAdmin(adminSession.toString())) {
            return "redirect:/admin/login";
        }
        return "admin/main";
    }

    // 상품 목록 조회
    @GetMapping("/books")
    public String bookList(
            @ModelAttribute BookSearchRequest request,
            @ModelAttribute PageRequest pageRequest,
            Model model) {
        
        PageResponse<BookListResponse> bookPage = bookCategoryService.getBookListForAdmin(request, pageRequest);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("pageInfo", bookPage);
        model.addAttribute("searchRequest", request);
        return "admin/book-list";
    }

    // 상품 등록 화면
    @GetMapping("/books/new")
    public String bookRegisterForm(Model model) {
        model.addAttribute("categories", bookCategoryService.getAllCategories());
        return "admin/book-register";
    }

    // 상품 등록 처리
    @PostMapping("/books")
    @ResponseBody
    public ApiResponse<String> bookRegister(@RequestBody BookRegisterRequest request) {
        bookCategoryService.registerBook(request);
        return ApiResponse.success("상품이 성공적으로 등록되었습니다.");
    }

    // 주문 목록 조회
    @GetMapping("/orders")
    public String orderList(
            @ModelAttribute OrderSearchRequest request,
            @ModelAttribute PageRequest pageRequest,
            Model model) {
        
        PageResponse<OrderListResponse> orderPage = orderPaymentService.getOrderListForAdmin(request, pageRequest);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("pageInfo", orderPage);
        model.addAttribute("searchRequest", request);
        return "admin/order-list";
    }

    // 주문 상세 조회
    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        OrderResponse order = orderPaymentService.getOrderDetail(orderId);
        model.addAttribute("order", order);
        return "admin/order-detail";
    }

    // 주문 상태 변경
    @PutMapping("/orders/{orderId}/status")
    @ResponseBody
    public ApiResponse<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        orderPaymentService.updateOrderStatus(orderId, request.getStatus());
        return ApiResponse.success("주문 상태가 성공적으로 변경되었습니다.");
    }

    // 회원 목록 조회
    @GetMapping("/members")
    public String memberList(
            @ModelAttribute MemberSearchRequest request,
            @ModelAttribute PageRequest pageRequest,
            Model model) {
        
        PageResponse<MemberResponse> memberPage = memberService.getMemberListForAdmin(request, pageRequest);
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("pageInfo", memberPage);
        model.addAttribute("searchRequest", request);
        return "admin/member-list";
    }

    // 회원 상세 조회
    @GetMapping("/members/{memberId}")
    public String memberDetail(@PathVariable String memberId, Model model) {
        MemberResponse member = memberService.getMemberDetail(memberId);
        model.addAttribute("member", member);
        return "admin/member-detail";
    }

    // 회원의 주문 목록 조회
    @GetMapping("/members/{memberId}/orders")
    public String memberOrderList(
            @PathVariable String memberId,
            @ModelAttribute PageRequest pageRequest,
            Model model) {
        PageResponse<OrderListResponse> orderPage = orderPaymentService.getOrderListByMember(memberId, pageRequest);
        MemberResponse member = memberService.getMemberDetail(memberId);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("pageInfo", orderPage);
        model.addAttribute("memberId", memberId);
        model.addAttribute("memberName", member.getMemberName());
        return "admin/member-orders";
    }

    // 재고 목록 조회
    @GetMapping("/inventory")
    public String inventoryList(
            @ModelAttribute InventorySearchRequest request,
            @ModelAttribute PageRequest pageRequest,
            Model model) {
        
        PageResponse<InventoryResponse> inventoryPage = bookCategoryService.getInventoryList(request, pageRequest);
        model.addAttribute("inventory", inventoryPage.getContent());
        model.addAttribute("pageInfo", inventoryPage);
        model.addAttribute("searchRequest", request);
        return "admin/inventory-list";
    }

    // 재고 수정
    @PutMapping("/inventory/{bookId}")
    @ResponseBody
    public ApiResponse<String> updateInventory(
            @PathVariable Long bookId,
            @RequestBody InventoryUpdateRequest request) {
        bookCategoryService.updateInventory(bookId, request.getQuantity());
        return ApiResponse.success("재고가 성공적으로 수정되었습니다.");
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // 총 상품 수
        stats.put("totalBooks", bookCategoryService.countBooks());
        // 오늘 주문 수
        stats.put("todayOrders", orderPaymentService.countOrdersByDate(LocalDate.now()));
        // 총 회원 수
        stats.put("totalMembers", memberService.countMembers());
        // 품절 상품 수
        stats.put("outOfStock", bookCategoryService.countOutOfStockBooks());
        return stats;
    }

    @GetMapping("/login")
    public String adminLoginForm() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String adminId, @RequestParam String password, HttpSession session, Model model) {
        Optional<com.example.fastcampusbookstore.entity.Admin> adminOpt = adminService.findByAdminId(adminId);
        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            session.setAttribute("adminId", adminId);
            return "redirect:/admin";
        } else {
            model.addAttribute("error", "관리자 정보가 일치하지 않습니다.");
            return "admin-login";
        }
    }
} 