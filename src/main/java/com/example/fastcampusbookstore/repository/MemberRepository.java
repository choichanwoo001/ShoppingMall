package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String>, JpaSpecificationExecutor<Member> {
    // Repository에서 하면 안 되는 것들:
    // - DTO 반환
    // - 비즈니스 로직 처리
    // - 복잡한 데이터 변환

    // Repository는 이런 것들만:
    // - Entity 저장/조회/수정/삭제
    // - 단순한 필터링
    // - 기본적인 쿼리 메서드

//    명시적 의도: "이 값은 없을 수 있다"를 타입으로 표현
//    Null Safety: NPE 방지
//    함수형 스타일: map, filter, orElse 등으로 우아한 처리
//    가독성 향상: 복잡한 null 체크를 간결하게
//
//    결론: Optional은 단순히 null을 피하기 위한 래퍼가 아니라,
//    "값의 부재 가능성"을 명시적으로 표현하고 안전하게 처리하기 위한 도구이다

    // Id로 회원 찾기
    Optional<Member> findByMemberId(String memberId);

    // Email로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 기존 회원 존재여부(Id)
    boolean existsByMemberId(String memberId);

    // 기존 회원 존재여부(Email)
    boolean existsByEmail(String email);

    // 2. 조건별 조회
    List<Member> findByMemberGrade(Member.MemberGrade memberGrade);
    List<Member> findByMemberStatus(Member.MemberStatus memberStatus);

    // 3. 날짜 범위 조회
    List<Member> findByJoinDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 4. 사용자 정의 쿼리
    @Query("SELECT m FROM Member m WHERE m.lastLoginDate < :date")
    List<Member> findInactiveMembers(@Param("date") LocalDateTime date);

    // 5. 이메일 도메인별 조회
    @Query("SELECT m FROM Member m WHERE m.email LIKE %:domain%")
    List<Member> findByEmailDomain(@Param("domain") String domain);
}
