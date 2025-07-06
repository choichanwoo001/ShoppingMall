package com.example.fastcampusbookstore.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberSearchRequest {
    private String memberId;
    private String memberStatus;
    private String email;
    private LocalDate startDate;
    private LocalDate endDate;
    private String memberGrade;
    private String memberName;
    private String sortBy; // memberId, memberName, joinDate
    private String sortOrder; // asc, desc
} 