package org.example.mainservice.course.userProfile.service;

import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.UUID;

public interface UserProfileService {
    UUID save(UserProfile userProfile, Long gradeId);
    void update(UserProfile userProfile);
    void delete(UUID id);
    UserProfile findById(UUID id);

    Page<UserProfile> getAllUserProfiles(int page, int size, Sort sort);
    void setNewGrade(UUID userId, long gradeId);

}
