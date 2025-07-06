package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class IdCheckRequest {

    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문, 숫자만 사용 가능합니다")
    private String memberId;
}

