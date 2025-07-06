package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.Member;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class MemberResponse {
    private String memberId;
    private String memberName;
    private String email;
    private String phone;
    private String address;
    private String memberGrade;
    private String memberStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginDate;

    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        response.memberId = member.getMemberId();
        response.memberName = member.getMemberName();
        response.email = member.getEmail();
        response.phone = member.getPhone();
        response.address = member.getAddress();
        response.memberGrade = member.getMemberGrade() != null ? member.getMemberGrade().toString() : null;
        response.memberStatus = member.getMemberStatus() != null ? member.getMemberStatus().toString() : null;
        response.joinDate = member.getJoinDate();
        response.lastLoginDate = member.getLastLoginDate();
        return response;
    }
}
