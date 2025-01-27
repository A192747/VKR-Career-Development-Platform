package org.example.mainservice.course.promotion.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.grade.service.internal.GradeRepository;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.promotion.service.internal.PromotionRepository;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userProfile.service.internal.UserProfileRepository;
import org.example.mainservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final GradeRepository gradeRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Long save(UUID userId, Long currentGrade, Long newGrade) {
        Promotion promotion = new Promotion();
        promotion.setUserProfile(findUserById(userId));
        promotion.setCurrentGrade(findGradeById(currentGrade));
        promotion.setNewGrade(findGradeById(newGrade));
        promotion.setPromotionDate(Instant.now());
        return promotionRepository.save(promotion).getId();
    }

    @Override
    public void update(Promotion promotion) {
        log.info("Update user with id = {}", promotion.getId());
        Promotion promotionValue = findPromotionById(promotion.getId());
        promotionValue.setPromotionDate(Instant.now());
        promotionValue.setNewGrade(promotion.getNewGrade());
        promotionValue.setCurrentGrade(promotion.getCurrentGrade());
        promotionValue.setUserProfile(promotion.getUserProfile());
        promotionRepository.save(promotionValue);
    }

    @Override
    public void delete(Long id) {
        log.info("Delete grade with id = {}", id);
        Promotion promotionValue = findPromotionById(id);
        promotionRepository.delete(promotionValue);
    }

    @Override
    public Promotion findById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Promotion with id =  %s not found".formatted(id))
        );
    }

    @Override
    public List<Promotion> getPromotionByUserId(UUID id) {
        log.info("Get Promotions with userid = {}", id);
        return findUserById(id).getPromotions();
    }

    @Override
    public Page<Promotion> getAllPromotions(int page, int size, Sort sort) {
        log.info("Get all Promotions");
        PageRequest pageable = PageRequest.of(page, size, sort);
        return promotionRepository.findAll(pageable);
    }


    private Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Promotion with id =  %s not found".formatted(id))
        );
    }

    private Grade findGradeById(long id) {
        return gradeRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Grade with id =  %s not found".formatted(id))
        );
    }

    private UserProfile findUserById(UUID id) {
        return userProfileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User with id =  %s not found".formatted(id))
        );
    }
}
