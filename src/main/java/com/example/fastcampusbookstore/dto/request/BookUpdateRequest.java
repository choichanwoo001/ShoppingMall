package com.example.fastcampusbookstore.dto.request;

import lombok.Data;

@Data
public class BookUpdateRequest {
    private String isbn;
    private String bookName;
    private String author;
    private String publisher;
    private String description;
    private Integer price;
    private String bookImage;
    private String previewPdf;
    private String bookSize;
    private Double rating;
    private Integer salesIndex;
    private String categoryTop;
    private String categoryMiddle;
    private String categoryBottom;
    private String salesStatus; // 판매중, 절판, 일시품절, 입고예정

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getBookImage() { return bookImage; }
    public void setBookImage(String bookImage) { this.bookImage = bookImage; }
    public String getBookSize() { return bookSize; }
    public void setBookSize(String bookSize) { this.bookSize = bookSize; }
    public String getPreviewPdf() { return previewPdf; }
    public void setPreviewPdf(String previewPdf) { this.previewPdf = previewPdf; }
} 