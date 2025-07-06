package com.example.fastcampusbookstore.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class PopularKeywordService {
    private final ConcurrentHashMap<String, AtomicInteger> keywordCount = new ConcurrentHashMap<>();

    // 검색어 카운트 증가
    public void increaseCount(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        keywordCount.computeIfAbsent(keyword.trim(), k -> new AtomicInteger(0)).incrementAndGet();
    }

    // 인기 검색어 Top N 반환
    public List<String> getTopKeywords(int n) {
        return keywordCount.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // (선택) 전체 카운트 반환
    public Map<String, Integer> getAllKeywordCounts() {
        return keywordCount.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
} 