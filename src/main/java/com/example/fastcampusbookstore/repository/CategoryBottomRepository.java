package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.CategoryBottom;
import com.example.fastcampusbookstore.entity.CategoryMiddle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBottomRepository extends JpaRepository<CategoryBottom, Integer> {

    // 특정 중분류의 활성화된 소분류 카테고리 조회
    List<CategoryBottom> findByMiddleCategoryAndIsActiveTrueOrderBySortOrder(CategoryMiddle middleCategory);

    // 중분류별 소분류 조회
    List<CategoryBottom> findByMiddleCategory(CategoryMiddle middleCategory);

    // 활성화 상태별 조회
    List<CategoryBottom> findByIsActive(Boolean isActive);
    
    // 카테고리 이름으로 조회
    Optional<CategoryBottom> findByCategoryName(String categoryName);
}