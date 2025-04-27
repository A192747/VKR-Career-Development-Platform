package org.example.mainservice.course.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.schedule.service.internal.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;


public interface ScheduleService {
    long save(Schedule schedule) throws BadRequestException, JsonProcessingException;
    void update(Schedule schedule) throws BadRequestException;
    void delete(Long id);
    void approve(Long scheduleId, UUID appoverId, Boolean approved) throws JsonProcessingException, BadRequestException;
    Schedule findById(Long id);
    Page<Schedule> getAllSchedules(int page, int size, Sort sort);
    List<Schedule> getAllScheduleByUserId(UUID userId);
}
