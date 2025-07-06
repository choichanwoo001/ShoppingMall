package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.common.PageRequest;
import com.example.fastcampusbookstore.dto.common.PageResponse;
import com.example.fastcampusbookstore.dto.request.*;
import com.example.fastcampusbookstore.dto.response.MemberResponse;
import com.example.fastcampusbookstore.entity.Member;
import com.example.fastcampusbookstore.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 1. 회원가입
    @Transactional
    public MemberResponse signup(MemberSignupRequest request) {

        // 유효성 검사
        validateSignupRequest(request);

        // DTO → Entity 변환
        Member member = convertToEntity(request);

        // 저장
        Member savedMember = memberRepository.save(member);

        // Entity → DTO 변환 후 반환
        return MemberResponse.from(savedMember);
    }

    // 2. 로그인
    @Transactional
    public MemberResponse login(MemberLoginRequest request) {

        // 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // 비밀번호 확인
        if (!BCrypt.checkpw(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 최종 로그인 시간 업데이트
        member.setLastLoginDate(LocalDateTime.now());
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    // 3. 아이디 중복 확인
    public boolean checkIdDuplicate(IdCheckRequest request) {

        // 아이디 형식 검증
        validateMemberId(request.getMemberId());

        return memberRepository.existsByMemberId(request.getMemberId());
    }

    // 4. 비밀번호 재설정
    @Transactional
    public void resetPassword(PasswordResetRequest request) {

        // 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 회원이 없습니다"));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();
        member.setPassword(BCrypt.hashpw(tempPassword, BCrypt.gensalt()));

        memberRepository.save(member);

        // 실제로는 이메일 발송 서비스 호출해야 함
        // emailService.sendTempPassword(member.getEmail(), tempPassword);
    }

    // 5. 회원 정보 수정
    @Transactional
    public MemberResponse updateMember(String memberId, MemberUpdateRequest request) {

        // 기존 회원 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // 수정 권한 검증
        validateUpdateRequest(request, member);

        // Entity 수정
        updateMemberFields(member, request);

        Member updatedMember = memberRepository.save(member);

        return MemberResponse.from(updatedMember);
    }

    @Transactional
    public void withdrawMember(String memberId) {

        // 회원 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // 탈퇴 가능 상태 확인
        validateWithdrawal(member);

        // 회원 상태를 탈퇴로 변경 (실제 삭제 대신 상태 변경)
        member.setMemberStatus(Member.MemberStatus.탈퇴);
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);

        // TODO: 관련 데이터 정리 (주문 내역은 보관, 개인정보는 마스킹 등)
        // cleanupMemberData(memberId);
    }

    public boolean checkEmailDuplicate(EmailCheckRequest request) {

        // 이메일 형식 검증
        validateEmail(request.getEmail());

        return memberRepository.existsByEmail(request.getEmail());
    }
    // === Private 헬퍼 메서드들 ===

    private void validateWithdrawal(Member member) {
        // 이미 탈퇴한 회원인지 확인
        if (member.getMemberStatus() == Member.MemberStatus.탈퇴) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다");
        }

        // TODO: 추가 탈퇴 조건 검사
        // - 진행 중인 주문이 있는지 확인
        // - 미해결 문의사항이 있는지 확인 등
    }

    private void validateSignupRequest(MemberSignupRequest request) {
        // 아이디 중복 확인
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }

        // 간단한 이메일 형식 검증
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }

    private Member convertToEntity(MemberSignupRequest request) {
        Member member = new Member();
        member.setMemberId(request.getMemberId());
        member.setMemberName(request.getMemberName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        member.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        member.setMemberGrade(Member.MemberGrade.일반);
        member.setMemberStatus(Member.MemberStatus.활성);
        member.setJoinDate(LocalDateTime.now());
        return member;
    }

    private void validateMemberId(String memberId) {
        if (memberId.length() < 4 || memberId.length() > 50) {
            throw new IllegalArgumentException("아이디는 4-50자 사이여야 합니다");
        }

        if (!memberId.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자만 사용 가능합니다");
        }
    }

    private void validateUpdateRequest(MemberUpdateRequest request, Member existingMember) {
        // 이메일 중복 확인 (본인 제외)
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    if (!member.getMemberId().equals(existingMember.getMemberId())) {
                        throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
                    }
                });
    }

    private void updateMemberFields(Member member, MemberUpdateRequest request) {
        member.setMemberName(request.getMemberName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
    }

    private String generateTempPassword() {
        return "TEMP" + System.currentTimeMillis();
    }

    // === 관리자 기능 ===

    // 관리자용 회원 목록 조회
    public PageResponse<MemberResponse> getMemberListForAdmin(MemberSearchRequest request, PageRequest pageRequest) {
        Specification<Member> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(request.getMemberId())) {
                predicates.add(cb.like(root.get("memberId"), "%" + request.getMemberId() + "%"));
            }
            if (StringUtils.hasText(request.getMemberStatus())) {
                predicates.add(cb.equal(root.get("memberStatus"), Member.MemberStatus.valueOf(request.getMemberStatus())));
            }
            if (StringUtils.hasText(request.getEmail())) {
                predicates.add(cb.like(root.get("email"), "%" + request.getEmail() + "%"));
            }
            if (StringUtils.hasText(request.getMemberGrade())) {
                predicates.add(cb.equal(root.get("memberGrade"), Member.MemberGrade.valueOf(request.getMemberGrade())));
            }
            if (StringUtils.hasText(request.getMemberName())) {
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
            .map(MemberResponse::from)
            .collect(Collectors.toList());
        return PageResponse.of(content, memberPage);
    }

    // 회원 상세 조회 (관리자용)
    public MemberResponse getMemberDetail(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        return MemberResponse.from(member);
    }
}