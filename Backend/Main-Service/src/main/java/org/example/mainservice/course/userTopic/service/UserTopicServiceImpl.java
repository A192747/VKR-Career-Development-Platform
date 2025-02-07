package org.example.mainservice.course.userTopic.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.promotion.service.internal.PromotionRepository;
import org.example.mainservice.course.topic.service.TopicService;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.userProfile.service.UserProfileService;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userTopic.service.internal.TopicStatus;
import org.example.mainservice.course.userTopic.service.internal.UserTopic;
import org.example.mainservice.course.userTopic.service.internal.UserTopicRepository;
import org.example.mainservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserTopicServiceImpl implements UserTopicService {

    private final UserTopicRepository userTopicRepository;
    private final UserProfileService userProfileService;
    private final PromotionRepository promotionRepository; //Иначе получается циклическая зависимость
    private final TopicService topicService;


    @Override
    public void save(UserProfile currentUserProfile, Grade newGrade, Topic topic, Promotion promotion) {
        UserTopic userTopic = new UserTopic();
        userTopic.setUserProfile(currentUserProfile);
        userTopic.setTopic(topic);
        userTopic.setPromotion(promotion);
        userTopic.setTopicStatus(TopicStatus.IN_PROCESS);
        userTopic.setUpdatedAt(Instant.now());
        userTopicRepository.save(userTopic);
    }

    @Override
    public void update(UserTopic userTopic) throws BadRequestException {
        log.info("Update user with id = {}", userTopic.getId());
        UserTopic userTopicValue = findById(userTopic.getId());

        userTopicValue.setTopicStatus(userTopic.getTopicStatus());
        userTopicValue.setUpdatedAt(Instant.now());
        userTopicValue.setUserProfile(userProfileService.findById(userTopic.getUserProfile().getId()));
        userTopicValue.setCommitLink(userTopic.getCommitLink());

        Promotion promotion = findPromotionById(userTopic.getPromotion().getId());
        isPromotionCorrect(promotion, userTopic.getUserProfile().getId());
        userTopicValue.setPromotion(promotion);

        Topic topic = topicService.findById(userTopic.getTopic().getId());
        isTopicCorrect(promotion, topic);
        userTopicValue.setTopic(topic);

        userTopicRepository.save(userTopicValue);
    }

    @Override
    public void updateMy(UserTopic userTopic, UUID userID) {
        log.info("Update userTopic with id = {}", userTopic.getId());
        UserTopic userTopicValue = findById(userTopic.getId());

        if (!userTopicValue.getUserProfile().getId().equals(userID))
            throw new AccessDeniedException("Вы пытаетесь изменить данные, которые вам не принадлежат!");

        userTopicValue.setTopicStatus(userTopic.getTopicStatus());
        userTopicValue.setUpdatedAt(Instant.now());
        userTopicValue.setCommitLink(userTopic.getCommitLink());

        userTopicRepository.save(userTopicValue);
    }


    private void isPromotionCorrect(Promotion promotion, UUID userId) throws BadRequestException {
        if (!promotion.getUserProfile().getId().equals(userId)) {
            throw new BadRequestException("Вы пытаетесь обновить данные, которые не относятся к данному пользователю в разделе promotion!");
        }
    }

    private void isTopicCorrect(Promotion promotion, Topic topic) throws BadRequestException {
        if (!promotion.getNewGrade().getTopics().contains(topic)) {
            throw new BadRequestException("Вы пытаетесь обновить данные, которые не относятся к topic-ам, которые доступны пользователю!");
        }
    }

    @Override
    public void delete(UserTopic userTopic) {
        log.info("Delete userTopic with id = {}", userTopic.getId());
        userTopicRepository.delete(userTopic);
    }

    @Override
    public UserTopic findById(Long id) {
        return userTopicRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("UserTopic with id =  %s not found".formatted(id))
        );
    }

    @Override
    public List<UserTopic> getAllUserTopicByUserId(UUID id) {
        UserProfile userProfile = userProfileService.findById(id);
        return userProfile.getUserTopics();
    }

    @Override
    public void setUserTopic(UUID userId, long userTopicId) {
        UserProfile userProfile = userProfileService.findById(userId);
        UserTopic userTopic = findById(userTopicId);
        userProfile.getUserTopics().add(userTopic);
        userProfileService.update(userProfile);
    }

    @Override
    public void setUserTopicStatus(long userTopicId, TopicStatus topicStatus) {
        UserTopic userTopic = findById(userTopicId);
        userTopic.setTopicStatus(topicStatus);
        userTopicRepository.save(userTopic);
    }


    @Override
    public Page<UserTopic> getAllUserTopic(int page, int size, Sort sort) {
        log.info("Get all userTopics");
        PageRequest pageable = PageRequest.of(page, size, sort);
        return userTopicRepository.findAll(pageable);
    }


    private Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Promotion with id =  %s not found".formatted(id))
        );
    }
}
