package com.example.minicodevault.service;

import com.example.minicodevault.model.CodingTest;

import java.math.BigDecimal;
import java.util.List;

public record LearningInput(
        String type,
        String title,
        String description,
        BigDecimal price,
        String status,
        boolean available,
        Integer maxDownloads,
        String ebookText,
        Integer accessDays,
        Integer durationMinutes,
        List<String> chapters,
        List<CodingTest.Question> questions) {}
