package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.request.CartAddRequest;
import com.example.fastcampusbookstore.dto.response.CartItemResponse;
import com.example.fastcampusbookstore.dto.response.CartSummaryResponse;
import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.entity.Cart;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.repository.BookRepository;
import com.example.fastcampusbookstore.repository.CartRepository;
import com.example.fastcampusbookstore.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    // 1. 장바구니 요약 조회
    public CartSummaryResponse getCartSummary(String memberId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        List<Cart> cartItems = cartRepository.findByMemberOrderByAddedDateDesc(member);

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList());

        return CartSummaryResponse.from(itemResponses);
    }

    // 2. 장바구니에 상품 추가
    @Transactional
    public void addToCart(String memberId, CartAddRequest request) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다"));

        // 판매 상태 확인
        if (book.getSalesStatus() != Book.SalesStatus.판매중) {
            throw new IllegalArgumentException("현재 판매하지 않는 상품입니다");
        }

        // 재고 확인
        if (book.getInventory() != null && book.getInventory().getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }

        // 이미 장바구니에 있는지 확인
        Optional<Cart> existingCart = cartRepository.findByMemberAndBook(member, book);

        if (existingCart.isPresent()) {
            // 기존 아이템 수량 증가
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
            cartRepository.save(cart);
        } else {
            // 새 아이템 추가
            Cart cart = new Cart();
            cart.setMember(member);
            cart.setBook(book);
            cart.setQuantity(request.getQuantity());
            cart.setAddedDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    // 3. 장바구니 수량 수정
    @Transactional
    public void updateCartItem(String memberId, Integer cartId, Integer quantity) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 아이템입니다"));

        // 본인 소유인지 확인
        if (!cart.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        // 재고 확인
        if (cart.getBook().getInventory() != null &&
                cart.getBook().getInventory().getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }

        cart.setQuantity(quantity);
        cartRepository.save(cart);
    }

    // 4. 장바구니 상품 삭제
    @Transactional
    public void removeCartItem(String memberId, Integer cartId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 아이템입니다"));

        // 본인 소유인지 확인
        if (!cart.getMember().getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        cartRepository.delete(cart);
    }

    // 5. 선택한 상품들 삭제
    @Transactional
    public void removeSelectedItems(String memberId, List<Integer> cartIds) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        List<Cart> carts = cartRepository.findAllById(cartIds);

        // 모든 아이템이 본인 소유인지 확인
        for (Cart cart : carts) {
            if (!cart.getMember().getMemberId().equals(member.getMemberId())) {
                throw new IllegalArgumentException("접근 권한이 없습니다");
            }
        }

        cartRepository.deleteAll(carts);
    }
}