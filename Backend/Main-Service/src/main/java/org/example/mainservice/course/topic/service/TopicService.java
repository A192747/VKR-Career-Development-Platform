package org.example.mainservice.course.topic.service;

import org.example.mainservice.course.topic.service.internal.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface TopicService {
    long save(Topic topic);
    void update(Topic topic);
    void delete(long id);
    Topic getTopicById(long id);
    Page<Topic> getAllTopics(int page, int size, Sort sort);

}
