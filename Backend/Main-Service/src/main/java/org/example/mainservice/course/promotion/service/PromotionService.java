package org.example.mainservice.course.promotion.service;

import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface PromotionService {
    Long save(UUID userId, Long currentGrade, Long newGrade);
    void update(Promotion promotion);
    void delete(Long id);
    Promotion findById(Long id);
    List<Promotion> getPromotionByUserId(UUID id);
    Page<Promotion> getAllPromotions(int page, int size, Sort sort);
}
