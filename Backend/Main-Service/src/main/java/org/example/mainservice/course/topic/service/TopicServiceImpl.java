package org.example.mainservice.course.topic.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.course.topic.service.internal.TopicRepository;
import org.example.mainservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    public long save(Topic topic) {
        log.info("Saving topic {}", topic);
        return topicRepository.save(topic).getId();
    }

    @Override
    public void update(Topic topic) {
        log.info("Update topic with id = {}", topic.getId());
        Topic topicValue = topicRepository.findById(topic.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Topic with id =  %s not found".formatted(topic.getId()))
        );
        topicValue.setName(topic.getName());
        topicRepository.save(topicValue);
    }

    @Override
    public void delete(long id) {
        log.info("Delete topic with id = {}", id);
        Topic topicValue = topicRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Topic with id =  %s not found".formatted(id))
        );
        topicRepository.delete(topicValue);
    }

    @Override
    public Topic getTopicById(long id) {
        log.info("Get topic with id = {}", id);
        return topicRepository.findById(id).orElseThrow(() -> {
            log.warn("Topic with id: {} not found ", id);
            return new ResourceNotFoundException("Topic with id =  %s not found".formatted(id));
        });
    }

    @Override
    public Page<Topic> getAllTopics(int page, int size, Sort sort) {
        log.info("Get all topics");
        PageRequest pageable = PageRequest.of(page, size);
        return topicRepository.findAll(pageable);
    }
}
