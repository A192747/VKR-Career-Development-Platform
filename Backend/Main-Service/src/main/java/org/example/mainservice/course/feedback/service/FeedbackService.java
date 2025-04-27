package org.example.mainservice.course.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.feedback.service.internal.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface FeedbackService {
    long save(Feedback feedback) throws BadRequestException, JsonProcessingException;
    void update(Feedback feedback);
    void delete(long id);
    Feedback findById(long id);
    Page<Feedback> getAllFeedbacks(int page, int size, Sort sort);
    List<Feedback> getAllFeedbacksByUserId(UUID userId);
}
