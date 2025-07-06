package com.example.fastcampusbookstore.controller;

import com.example.fastcampusbookstore.service.PopularKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/popular-keywords")
@RequiredArgsConstructor
public class PopularKeywordController {
    private final PopularKeywordService popularKeywordService;

    // 검색어 카운트 증가 (검색 API에서 호출)
    @PostMapping("/increase")
    public void increaseKeyword(@RequestParam String keyword) {
        popularKeywordService.increaseCount(keyword);
    }

    // 인기 검색어 Top N 반환
    @GetMapping("/top")
    public List<String> getTopKeywords(@RequestParam(defaultValue = "10") int size) {
        return popularKeywordService.getTopKeywords(size);
    }

    // (선택) 전체 카운트 반환
    @GetMapping("/all")
    public Map<String, Integer> getAllKeywordCounts() {
        return popularKeywordService.getAllKeywordCounts();
    }
} 