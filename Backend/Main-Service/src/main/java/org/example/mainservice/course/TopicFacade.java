package org.example.mainservice.course;

import lombok.RequiredArgsConstructor;
import org.example.mainservice.course.topic.service.TopicService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicFacade {
    private final TopicService topicService;

    public String getTopicName(Long id) {
        return topicService.findById(id).getName();
    }
}
