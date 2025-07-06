package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class MemberLoginRequest {

    @NotBlank(message = "아이디는 필수입니다")
    private String memberId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private boolean rememberMe = false; // 아이디 기억하기
}
