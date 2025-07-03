package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.request.*;
import com.example.fastcampusbookstore.dto.response.MemberResponse;
import com.example.fastcampusbookstore.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(
            @Valid @RequestBody MemberSignupRequest request) {

        log.info("회원가입 요청: memberId={}", request.getMemberId());

        try {
            MemberResponse response = memberService.signup(request);
            log.info("회원가입 성공: memberId={}", response.getMemberId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다.", response));

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponse>> login(
            @Valid @RequestBody MemberLoginRequest request,
            HttpSession session) {

        log.info("로그인 요청: memberId={}", request.getMemberId());

        try {
            MemberResponse response = memberService.login(request);

            // 세션에 로그인 정보 저장
            session.setAttribute("loginMember", response);
            session.setAttribute("memberId", response.getMemberId());
            session.setAttribute("memberName", response.getMemberName()); // 추가
            session.setAttribute("memberGrade", response.getMemberGrade()); // 추가
            session.setAttribute("isLoggedIn", true); // 추가

            log.info("로그인 성공: memberId={}", response.getMemberId());

            return ResponseEntity.ok(ApiResponse.success("로그인되었습니다.", response));

        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 처리 중 오류가 발생했습니다."));
        }
    }

    // 3. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");
        log.info("로그아웃 요청: memberId={}", memberId);

        // 세션 무효화
        session.invalidate();

        log.info("로그아웃 완료: memberId={}", memberId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }

    // 4. 아이디 중복 확인
    @PostMapping("/check-id")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkIdDuplicate(
            @Valid @RequestBody IdCheckRequest request) {

        log.info("아이디 중복 확인 요청: memberId={}", request.getMemberId());

        try {
            boolean isDuplicate = memberService.checkIdDuplicate(request);

            Map<String, Boolean> result = new HashMap<>();
            result.put("isDuplicate", isDuplicate);
            result.put("isAvailable", !isDuplicate);

            String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";

            return ResponseEntity.ok(ApiResponse.success(message, result));

        } catch (IllegalArgumentException e) {
            log.warn("아이디 중복 확인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("아이디 중복 확인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("아이디 중복 확인 중 오류가 발생했습니다."));
        }
    }

    // 5. 비밀번호 재설정 요청
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        log.info("비밀번호 재설정 요청: email={}", request.getEmail());

        try {
            memberService.resetPassword(request);
            log.info("비밀번호 재설정 이메일 발송 완료: email={}", request.getEmail());

            return ResponseEntity.ok(
                    ApiResponse.success("입력하신 이메일로 임시 비밀번호를 발송했습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("비밀번호 재설정 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("비밀번호 재설정 처리 중 오류가 발생했습니다."));
        }
    }

    // 6. 회원 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyInfo(
            @Valid @RequestBody MemberUpdateRequest request,
            HttpSession session) {

        String memberId = (String) session.getAttribute("memberId");

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("로그인이 필요합니다."));
        }

        log.info("회원 정보 수정 요청: memberId={}", memberId);

        try {
            MemberResponse response = memberService.updateMember(memberId, request);

            // 세션 정보 업데이트
            session.setAttribute("loginMember", response);

            log.info("회원 정보 수정 완료: memberId={}", memberId);

            return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다.", response));

        } catch (IllegalArgumentException e) {
            log.warn("회원 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("회원 정보 수정 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원 정보 수정 중 오류가 발생했습니다."));
        }
    }

    // 7. 로그인 상태 확인
    @GetMapping("/login-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLoginStatus(HttpSession session) {

        MemberResponse loginMember = (MemberResponse) session.getAttribute("loginMember");

        Map<String, Object> status = new HashMap<>();
        status.put("isLoggedIn", loginMember != null);

        if (loginMember != null) {
            status.put("member", loginMember);
        }

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    // === 예외 처리 ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}