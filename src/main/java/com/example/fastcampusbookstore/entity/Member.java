package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Members")
@Data
@EqualsAndHashCode(exclude = {"orders", "reviews", "cartItems", "recentViews"})
@ToString(exclude = {"orders", "reviews", "cartItems", "recentViews"})
public class Member {

    @Id
    @Column(name = "member_id", length = 50)
    private String memberId;

    @Column(name = "member_name", length = 100)
    private String memberName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_grade")
    private MemberGrade memberGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status")
    private MemberStatus memberStatus;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cart> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecentView> recentViews = new ArrayList<>();

    // Enum 정의
    public enum MemberGrade {
        일반, 실버, 골드, VIP
    }

    public enum MemberStatus {
        활성, 비활성, 휴면, 탈퇴
    }
}
