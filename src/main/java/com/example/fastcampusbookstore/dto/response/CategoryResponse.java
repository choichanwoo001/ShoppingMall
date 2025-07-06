package com.example.fastcampusbookstore.dto.response;

import com.example.fastcampusbookstore.entity.CategoryTop;
import com.example.fastcampusbookstore.entity.CategoryMiddle;
import com.example.fastcampusbookstore.entity.CategoryBottom;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponse {
    private Integer categoryId;
    private String categoryName;
    private Integer sortOrder;
    private Boolean isActive;
    private List<CategoryResponse> subCategories;

    // 대분류 → DTO 변환
    public static CategoryResponse fromTop(CategoryTop category) {
        CategoryResponse response = new CategoryResponse();
        response.categoryId = category.getCategoryId();
        response.categoryName = category.getCategoryName();
        response.sortOrder = category.getSortOrder();
        response.isActive = category.getIsActive();

        if (category.getMiddleCategories() != null) {
            response.subCategories = category.getMiddleCategories().stream()
                    .filter(middle -> middle.getIsActive() != null && middle.getIsActive())
                    .map(CategoryResponse::fromMiddle)
                    .collect(Collectors.toList());
        }

        return response;
    }

    // 중분류 → DTO 변환
    public static CategoryResponse fromMiddle(CategoryMiddle category) {
        CategoryResponse response = new CategoryResponse();
        response.categoryId = category.getCategoryId();
        response.categoryName = category.getCategoryName();
        response.sortOrder = category.getSortOrder();
        response.isActive = category.getIsActive();

        if (category.getBottomCategories() != null) {
            response.subCategories = category.getBottomCategories().stream()
                    .filter(bottom -> bottom.getIsActive() != null && bottom.getIsActive())
                    .map(CategoryResponse::fromBottom)
                    .collect(Collectors.toList());
        }

        return response;
    }

    // 소분류 → DTO 변환
    public static CategoryResponse fromBottom(CategoryBottom category) {
        CategoryResponse response = new CategoryResponse();
        response.categoryId = category.getCategoryId();
        response.categoryName = category.getCategoryName();
        response.sortOrder = category.getSortOrder();
        response.isActive = category.getIsActive();
        return response;
    }
}
