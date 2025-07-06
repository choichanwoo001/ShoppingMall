package com.example.fastcampusbookstore.service;

import com.example.fastcampusbookstore.dto.response.CategoryResponse;
import com.example.fastcampusbookstore.dto.response.CategoryTreeResponse;
import com.example.fastcampusbookstore.entity.CategoryBottom;
import com.example.fastcampusbookstore.entity.CategoryMiddle;
import com.example.fastcampusbookstore.entity.CategoryTop;
import com.example.fastcampusbookstore.repository.CategoryBottomRepository;
import com.example.fastcampusbookstore.repository.CategoryMiddleRepository;
import com.example.fastcampusbookstore.repository.CategoryTopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryTopRepository categoryTopRepository;
    private final CategoryMiddleRepository categoryMiddleRepository;
    private final CategoryBottomRepository categoryBottomRepository;

    // 1. 전체 카테고리 트리 조회
    public CategoryTreeResponse getAllCategoriesTree() {

        List<CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();

        List<CategoryResponse> categoryResponses = topCategories.stream()
                .map(CategoryResponse::fromTop)
                .collect(Collectors.toList());

        return new CategoryTreeResponse(categoryResponses);
    }

    // 2. 대분류 카테고리 조회
    public List<CategoryResponse> getTopCategories() {

        List<CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();

        return topCategories.stream()
                .map(CategoryResponse::fromTop)
                .collect(Collectors.toList());
    }

    // 3. 중분류 카테고리 조회
    public List<CategoryResponse> getMiddleCategories(Integer topCategoryId) {

        CategoryTop topCategory = categoryTopRepository.findById(topCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대분류 카테고리입니다"));

        List<CategoryMiddle> middleCategories = categoryMiddleRepository
                .findByTopCategoryAndIsActiveTrueOrderBySortOrder(topCategory);

        return middleCategories.stream()
                .map(CategoryResponse::fromMiddle)
                .collect(Collectors.toList());
    }

    // 4. 소분류 카테고리 조회
    public List<CategoryResponse> getBottomCategories(Integer middleCategoryId) {

        CategoryMiddle middleCategory = categoryMiddleRepository.findById(middleCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 중분류 카테고리입니다"));

        List<CategoryBottom> bottomCategories = categoryBottomRepository
                .findByMiddleCategoryAndIsActiveTrueOrderBySortOrder(middleCategory);

        return bottomCategories.stream()
                .map(CategoryResponse::fromBottom)
                .collect(Collectors.toList());
    }

    // 5. 모든 카테고리 조회 (관리자용)
    public List<CategoryResponse> getAllCategories() {
        List<CategoryTop> topCategories = categoryTopRepository.findByIsActiveTrueOrderBySortOrder();
        
        return topCategories.stream()
                .map(CategoryResponse::fromTop)
                .collect(Collectors.toList());
    }
}