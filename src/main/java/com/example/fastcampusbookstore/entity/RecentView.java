package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RecentViews")
@Data
@EqualsAndHashCode(exclude = {"member", "book"})
@ToString(exclude = {"member", "book"})
public class RecentView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    private Integer viewId;

    @Column(name = "view_date")
    private LocalDateTime viewDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id2")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id2")
    private Book book;
}
