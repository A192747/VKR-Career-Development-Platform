package org.example.mainservice.course.schedule.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.promotion.service.PromotionService;
import org.example.mainservice.course.schedule.service.internal.Schedule;
import org.example.mainservice.course.schedule.service.internal.ScheduleRepository;
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
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserProfileService userProfileService;
    private final PromotionService promotionService;


    @Override
    public long save(Schedule schedule) throws BadRequestException {
        log.info("Save schedule {}", schedule);
        schedule.setReady(false);
        schedule.setPromotion(promotionService.findById(schedule.getPromotion().getId()));

        schedule.setReviewedUserProfile(userProfileService.findById(schedule.getReviewedUserProfile().getId()));
        schedule.setReviewerUserProfile(userProfileService.findById(schedule.getReviewerUserProfile().getId()));

        isUserCorrect(schedule);
        isPromotionCorrect(schedule);
        isDateCorrect(schedule);

        return scheduleRepository.save(schedule).getId();
    }

    private void isDateCorrect(Schedule schedule) throws BadRequestException {
        if (schedule.getBeginAt().isBefore(Instant.now()) ||
                schedule.getFinishedAt().isBefore(Instant.now()) ||
                schedule.getFinishedAt().isBefore(schedule.getBeginAt())) {
            throw new BadRequestException("Некорректная дата!");
        }
    }

    @Override
    public void update(Schedule schedule) throws BadRequestException {
        Schedule scheduleValue = findById(schedule.getId());
        log.info("Update schedule with id = {}", scheduleValue.getId());
        scheduleValue.setReviewedUserProfile(userProfileService.findById(schedule.getReviewedUserProfile().getId()));
        scheduleValue.setReviewerUserProfile(userProfileService.findById(schedule.getReviewerUserProfile().getId()));
        scheduleValue.setPromotion(promotionService.findById(schedule.getPromotion().getId()));

        scheduleValue.setReady(schedule.isReady());
        scheduleValue.setComment(schedule.getComment());
        scheduleValue.setName(schedule.getName());
        scheduleValue.setBeginAt(schedule.getBeginAt());
        scheduleValue.setFinishedAt(schedule.getFinishedAt());
        scheduleValue.setInterviewFormat(schedule.getInterviewFormat());

        isUserCorrect(scheduleValue);
        isPromotionCorrect(scheduleValue);
        isDateCorrect(scheduleValue);

        scheduleRepository.save(scheduleValue);
    }

    private void isPromotionCorrect(Schedule schedule) throws BadRequestException {
        if (!schedule.getPromotion().getUserProfile().equals(schedule.getReviewedUserProfile()))
            throw new BadRequestException("Promotion не относится к данному пользователю!");
    }

    private void isUserCorrect(Schedule schedule) throws BadRequestException {
        if (schedule.getReviewedUserProfile().equals(schedule.getReviewerUserProfile()))
            throw new BadRequestException("Нельзя записываться к самому себе!");
    }

    @Override
    public void delete(Long id) {
        log.info("Delete schedule with id = {}", id);
        Schedule schedule = findById(id);
        scheduleRepository.delete(schedule);
    }

    @Override
    public Page<Schedule> getAllSchedules(int page, int size, Sort sort) {
        log.info("Get all Schedules");
        PageRequest pageable = PageRequest.of(page, size);
        return scheduleRepository.findAll(pageable);
    }

    @Override
    public List<Schedule> getAllFeedbacksByUserId(UUID userId) {
        UserProfile userProfile = userProfileService.findById(userId);

        List<Schedule> mySchedule = userProfile.getScheduleGivenReviews();
        mySchedule.addAll(userProfile.getScheduleReceivedReviews());
        return mySchedule;
    }


    @Override
    public Schedule findById(Long id) {
        ResourceNotFoundException exception = new ResourceNotFoundException("Schedule with id =  %s not found".formatted(id));
        if(id == null)
            throw exception;
        return scheduleRepository.findById(id).orElseThrow(() -> exception);
    }
}
