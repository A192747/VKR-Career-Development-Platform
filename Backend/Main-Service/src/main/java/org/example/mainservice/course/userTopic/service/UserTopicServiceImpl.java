package org.example.mainservice.course.userTopic.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.grade.service.internal.GradeRepository;
import org.example.mainservice.course.promotion.PromotionController;
import org.example.mainservice.course.promotion.service.internal.Promotion;
import org.example.mainservice.course.promotion.service.internal.PromotionRepository;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.topic.service.internal.TopicRepository;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userProfile.service.internal.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final PromotionRepository promotionRepository;
    private final TopicRepository topicRepository;

    @Override
    public Long save(UserTopic userTopic) throws BadRequestException {
        userTopic.setTopicStatus(TopicStatus.NOT_STARTED);
        userTopic.setUpdatedAt(Instant.now());

        Promotion promotion = findPromotionById(userTopic.getPromotion().getId());
        isPromotionCorrect(promotion, userTopic.getUserProfile().getId());

        Topic topic = findTopicById(userTopic.getTopic().getId());
        isTopicCorrect(promotion, topic);

        log.info("Saving userProfile: {}", userTopic);
        return userTopicRepository.save(userTopic).getId();
    }

    @Override
    public void update(UserTopic userTopic) throws BadRequestException {
        log.info("Update user with id = {}", userTopic.getId());
        UserTopic userTopicValue = findUserTopicById(userTopic.getId());

        userTopicValue.setTopicStatus(userTopic.getTopicStatus());
        userTopicValue.setUpdatedAt(Instant.now());
        userTopicValue.setUserProfile(findUserProfileById(userTopic.getUserProfile().getId()));
        userTopicValue.setCommitLink(userTopic.getCommitLink());

        Promotion promotion = findPromotionById(userTopic.getPromotion().getId());
        isPromotionCorrect(promotion, userTopic.getUserProfile().getId());
        userTopicValue.setPromotion(promotion);

        Topic topic = findTopicById(userTopic.getTopic().getId());
        isTopicCorrect(promotion, topic);
        userTopicValue.setTopic(topic);

        userTopicRepository.save(userTopicValue);
    }

    @Override
    public void updateMy(UserTopic userTopic, UUID userID) {
        log.info("Update userTopic with id = {}", userTopic.getId());
        UserTopic userTopicValue = findUserTopicById(userTopic.getId());

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
    public void delete(Long id) {
        log.info("Delete userTopic with id = {}", id);
        UserTopic userTopicValue = findUserTopicById(id);
        userTopicRepository.delete(userTopicValue);
    }

    @Override
    public UserTopic getById(Long id) {
        return findUserTopicById(id);
    }

    @Override
    public List<UserTopic> getAllUserTopicByUserId(UUID id) {
        UserProfile userProfile = findUserProfileById(id);
        return userProfile.getUserTopics();
    }

    @Override
    public void setUserTopic(UUID userId, long userTopicId) {
        UserProfile userProfile = findUserProfileById(userId);
        UserTopic userTopic = findUserTopicById(userTopicId);
        userProfile.getUserTopics().add(userTopic);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void setUserTopicStatus(long userTopicId, TopicStatus topicStatus) {
        UserTopic userTopic = findUserTopicById(userTopicId);
        userTopic.setTopicStatus(topicStatus);
        userTopicRepository.save(userTopic);
    }


    @Override
    public Page<UserTopic> getAllUserTopic(int page, int size, Sort sort) {
        log.info("Get all userTopics");
        PageRequest pageable = PageRequest.of(page, size, sort);
        return userTopicRepository.findAll(pageable);
    }


    private UserTopic findUserTopicById(Long id) {
        return userTopicRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("UserTopic with id =  %s not found".formatted(id))
        );
    }

    private Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Promotion with id =  %s not found".formatted(id))
        );
    }

    private UserProfile findUserProfileById(UUID id) {
        return userProfileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("UserProfile with id =  %s not found".formatted(id))
        );
    }

    private Topic findTopicById(Long id) {
        return topicRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Topic with id =  %s not found".formatted(id))
        );
    }
}
