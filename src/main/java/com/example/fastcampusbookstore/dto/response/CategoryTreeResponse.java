package com.example.fastcampusbookstore.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CategoryTreeResponse {
    private List<CategoryResponse> categories;

    public CategoryTreeResponse(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
