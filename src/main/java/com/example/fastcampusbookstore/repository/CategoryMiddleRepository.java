package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.CategoryMiddle;
import com.example.fastcampusbookstore.entity.CategoryTop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryMiddleRepository extends JpaRepository<CategoryMiddle, Integer> {

    // 특정 대분류의 활성화된 중분류 카테고리 조회
    List<CategoryMiddle> findByTopCategoryAndIsActiveTrueOrderBySortOrder(CategoryTop topCategory);

    // 대분류별 중분류 조회
    List<CategoryMiddle> findByTopCategory(CategoryTop topCategory);

    // 활성화 상태별 조회
    List<CategoryMiddle> findByIsActive(Boolean isActive);
    
    // 카테고리 이름으로 조회
    Optional<CategoryMiddle> findByCategoryName(String categoryName);
}