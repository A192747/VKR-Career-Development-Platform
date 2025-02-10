package org.example.mainservice.course.feedback.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.feedback.service.internal.Feedback;
import org.example.mainservice.course.feedback.service.internal.FeedbackRepository;
import org.example.mainservice.course.promotion.service.PromotionService;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.userProfile.service.UserProfileService;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
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
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserProfileService userProfileService;
    private final PromotionService promotionService;


    @Override
    public long save(Feedback feedback) throws BadRequestException {
        log.info("Save feedback {}", feedback);
        feedback.setCreatedAt(Instant.now());
        Promotion promotion = promotionService.findById(feedback.getPromotion().getId());
        feedback.setPromotion(promotion);
        UserProfile userReviewedProfile = userProfileService.findById(feedback.getReviewedUserProfile().getId());
        if (!userReviewedProfile.getPromotions().contains(promotion))
            throw new BadRequestException("Promotion не относится к данному пользователю");
        feedback.setReviewedUserProfile(userReviewedProfile);
        UserProfile userReviewerProfile = userProfileService.findById(feedback.getReviewerUserProfile().getId());
        feedback.setReviewerUserProfile(userReviewerProfile);
        if (userReviewerProfile.equals(userReviewedProfile))
            throw new BadRequestException("Отзыв не может быть дан самому себе!");

        return feedbackRepository.save(feedback).getId();
    }

    @Override
    public void update(Feedback feedback) {
        log.info("Update feedback with id = {}", feedback.getId());
        Feedback feedbackValue = findById(feedback.getId());
        feedbackValue.setConfirmed(feedback.isConfirmed());
        feedbackValue.setRelatedId(feedback.getRelatedId());
        feedbackValue.setRelatedType(feedback.getRelatedType());
        feedbackValue.setCreatedAt(Instant.now());
        feedbackValue.setPromotion(promotionService.findById(feedback.getPromotion().getId()));
        feedbackValue.setReviewedUserProfile(userProfileService.findById(feedback.getReviewedUserProfile().getId()));
        feedbackValue.setReviewerUserProfile(userProfileService.findById(feedback.getReviewerUserProfile().getId()));
        feedbackValue.setText(feedback.getText());
        feedbackRepository.save(feedbackValue);
    }

    @Override
    public void delete(long id) {
        log.info("Delete feedback with id = {}", id);
        Feedback feedbackValue = findById(id);
        feedbackRepository.delete(feedbackValue);
    }

    @Override
    public Page<Feedback> getAllFeedbacks(int page, int size, Sort sort) {
        log.info("Get all feedbacks");
        PageRequest pageable = PageRequest.of(page, size);
        return feedbackRepository.findAll(pageable);
    }

    @Override
    public List<Feedback> getAllFeedbacksByUserId(UUID userId) {
        log.info("Get all user`s feedbacks");
        UserProfile profile = userProfileService.findById(userId);
        return profile.getReceivedReviews();
    }


    @Override
    public Feedback findById(long id) {
        return feedbackRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Feedback with id =  %s not found".formatted(id))
        );
    }
}
