package com.example.fastcampusbookstore.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Admins")
@Data
public class Admin {

    @Id
    @Column(name = "admin_id", length = 50)
    private String adminId;

    @Column(name = "admin_name", length = 100)
    private String adminName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private AdminRole role;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum 정의
    public enum AdminRole {
        슈퍼관리자, 일반관리자
    }
}