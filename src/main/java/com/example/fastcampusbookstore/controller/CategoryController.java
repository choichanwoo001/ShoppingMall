package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.dto.common.ApiResponse;
import com.example.fastcampusbookstore.dto.response.CategoryResponse;
import com.example.fastcampusbookstore.dto.response.CategoryTreeResponse;
import com.example.fastcampusbookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    // 1. 전체 카테고리 트리 조회 (대, 중, 소분류)
    @GetMapping
    public ResponseEntity<ApiResponse<CategoryTreeResponse>> getAllCategories() {

        log.info("전체 카테고리 조회 요청");

        try {
            CategoryTreeResponse response = categoryService.getAllCategoriesTree();
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("카테고리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    // 2. 대분류 카테고리 조회
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getTopCategories() {

        log.info("대분류 카테고리 조회 요청");

        try {
            List<CategoryResponse> response = categoryService.getTopCategories();
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("대분류 카테고리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("대분류 카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    // 3. 중분류 카테고리 조회 (특정 대분류 하위)
    @GetMapping("/middle/{topCategoryId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getMiddleCategories(
            @PathVariable Integer topCategoryId) {

        log.info("중분류 카테고리 조회 요청: topCategoryId={}", topCategoryId);

        try {
            List<CategoryResponse> response = categoryService.getMiddleCategories(topCategoryId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("중분류 카테고리 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("중분류 카테고리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("중분류 카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    // 4. 소분류 카테고리 조회 (특정 중분류 하위)
    @GetMapping("/bottom/{middleCategoryId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getBottomCategories(
            @PathVariable Integer middleCategoryId) {

        log.info("소분류 카테고리 조회 요청: middleCategoryId={}", middleCategoryId);

        try {
            List<CategoryResponse> response = categoryService.getBottomCategories(middleCategoryId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("소분류 카테고리 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("소분류 카테고리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("소분류 카테고리 조회 중 오류가 발생했습니다."));
        }
    }
}