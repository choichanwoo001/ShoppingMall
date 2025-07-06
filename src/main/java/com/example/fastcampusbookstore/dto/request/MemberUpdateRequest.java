package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class MemberUpdateRequest {

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
