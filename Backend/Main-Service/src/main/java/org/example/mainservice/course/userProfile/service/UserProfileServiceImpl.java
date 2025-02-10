package org.example.mainservice.course.userProfile.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.course.grade.service.GradeService;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.userProfile.service.internal.UserProfile;
import org.example.mainservice.course.userProfile.service.internal.UserProfileRepository;
import org.example.mainservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final GradeService gradeService;

    @Override
    public UUID save(UserProfile userProfile, Long gradeId) {
        userProfile.setGrade(gradeService.findById(gradeId));
        log.info("Saving userProfile: {}", userProfile);
        return userProfileRepository.save(userProfile).getId();
    }

    @Override
    public void update(UserProfile userProfile) {
        log.info("Update user with id = {}", userProfile.getId());
        UserProfile userProfileValue = findById(userProfile.getId());
        userProfileValue.setFirstName(userProfile.getFirstName());
        userProfileValue.setLastName(userProfile.getLastName());
        userProfileValue.setEmail(userProfile.getEmail());
        userProfileValue.setDateOfBirth(userProfile.getDateOfBirth());
        userProfileRepository.save(userProfileValue);
    }

    @Override
    public void delete(UUID id) {
        log.info("Delete grade with id = {}", id);
        UserProfile userProfileValue = findById(id);
        userProfileRepository.delete(userProfileValue);
    }

    @Override
    public UserProfile findById(UUID id) {
        log.info("Get user with id = {}", id);
        return userProfileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Grade with id =  %s not found".formatted(id))
        );
    }

    @Override
    public Page<UserProfile> getAllUserProfiles(int page, int size, Sort sort) {
        log.info("Get all userProfiles");
        PageRequest pageable = PageRequest.of(page, size, sort);
        return userProfileRepository.findAll(pageable);
    }

    @Override
    public void setNewGrade(UUID userId, long gradeId) {
        UserProfile userProfileValue = findById(userId);
        Grade grade = gradeService.findById(gradeId);
        userProfileValue.setGrade(grade);
        userProfileRepository.save(userProfileValue);
        gradeService.save(grade);
    }

}
