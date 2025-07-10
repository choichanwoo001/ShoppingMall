package com.example.fastcampusbookstore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Books")
@Data
@EqualsAndHashCode(exclude = {"category", "inventory", "reviews", "cartItems", "orderDetails", "bestsellers", "recentViews"})
@ToString(exclude = {"category", "inventory", "reviews", "cartItems", "orderDetails", "bestsellers", "recentViews"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "book_name", length = 200)
    private String bookName;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "book_size", length = 50)
    private String bookSize;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "book_image", length = 255)
    private String bookImage;

    @Column(name = "preview_pdf", length = 255)
    private String previewPdf;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "sales_index")
    private Integer salesIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_status")
    private SalesStatus salesStatus;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "register_date")
    private LocalDateTime registerDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id2")
    private CategoryBottom category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_top_id")
    private CategoryTop categoryTop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_middle_id")
    private CategoryMiddle categoryMiddle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_bottom_id")
    private CategoryBottom categoryBottom;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cart> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bestseller> bestsellers = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecentView> recentViews = new ArrayList<>();

    // Enum 정의
    public enum SalesStatus {
        판매중, 절판, 일시품절, 입고예정
    }

    public String getBookImage() { return bookImage; }
    public void setBookImage(String bookImage) { this.bookImage = bookImage; }
    public String getBookSize() { return bookSize; }
    public void setBookSize(String bookSize) { this.bookSize = bookSize; }
    public String getPreviewPdf() { return previewPdf; }
    public void setPreviewPdf(String previewPdf) { this.previewPdf = previewPdf; }
}
