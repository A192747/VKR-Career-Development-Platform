package org.example.mainservice.userInteraction.userProfile.service;

import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.userInteraction.userProfile.service.internal.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface UserProfileService {
    UUID save(UserProfile userProfile);
    void update(UserProfile userProfile);
    void delete(UUID id);
    UserProfile getUserProfileById(UUID id);

    Page<UserProfile> getAllUserProfiles(int page, int size, Sort sort);
    void setNewGrade(UUID userId, long gradeId);
}
