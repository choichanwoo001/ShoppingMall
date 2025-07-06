package com.example.fastcampusbookstore.repository;

import com.example.fastcampusbookstore.entity.CategoryTop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryTopRepository extends JpaRepository<CategoryTop, Integer> {

    // 활성화된 대분류 카테고리 조회 (정렬순서별)
    List<CategoryTop> findByIsActiveTrueOrderBySortOrder();

    // 활성화 상태별 조회
    List<CategoryTop> findByIsActive(Boolean isActive);
    
    // 카테고리 이름으로 조회
    Optional<CategoryTop> findByCategoryName(String categoryName);
}