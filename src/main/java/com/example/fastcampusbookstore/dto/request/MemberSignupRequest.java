package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class MemberSignupRequest {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문, 숫자만 사용 가능합니다")
    private String memberId;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String passwordConfirm;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String memberName;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    @NotBlank(message = "연락처는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처는 010-0000-0000 형식이어야 합니다")
    private String phone;

    @NotBlank(message = "주소는 필수입니다")
    private String address;
}
