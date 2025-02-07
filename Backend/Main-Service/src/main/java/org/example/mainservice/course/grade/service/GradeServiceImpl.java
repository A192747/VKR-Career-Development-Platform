package org.example.mainservice.course.grade.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.grade.service.internal.GradeRepository;
import org.example.mainservice.course.topic.service.TopicService;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.example.mainservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final TopicService topicService;

    @Override
    public long save(Grade grade) {
        return gradeRepository.save(grade).getId();
    }

    @Override
    public void update(Grade grade) {
        log.info("Update grade with id = {}", grade.getId());
        Grade gradeValue = findById(grade.getId());
        gradeValue.setName(grade.getName());
        gradeRepository.save(gradeValue);
    }

    @Override
    public void delete(long id) {
        log.info("Delete grade with id = {}", id);
        Grade gradeValue = findById(id);
        gradeRepository.delete(gradeValue);
    }

    @Override
    public Grade findById(long id) {
        log.info("Get grade with id = {}", id);
        return gradeRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Grade with id =  %s not found".formatted(id))
        );
    }

    @Override
    public Page<Grade> getAllGrades(int page, int size, Sort sort) {
        log.info("Get all grades");
        PageRequest pageable = PageRequest.of(page, size, sort);
        return gradeRepository.findAll(pageable);
    }

    @Override
    public List<Topic> getTopicsForGrade(long id) {
        Grade gradeValue = findById(id);
        return gradeValue.getTopics();
    }

    @Override
    public void saveTopic(long gradeId, long topicId) {
        Grade gradeValue = findById(gradeId);
        Topic topicValue = topicService.findById(topicId);
        gradeValue.getTopics().add(topicValue);
        gradeRepository.save(gradeValue);
    }

    @Override
    public void deleteTopic(long gradeId, long topicId) {
        Grade gradeValue = findById(gradeId);
        Topic topicValue = topicService.findById(topicId);
        if (!gradeValue.getTopics().contains(topicValue)) {
            throw new ResourceNotFoundException("Topic not found in grade with id = " + gradeId);
        }
        gradeValue.getTopics().remove(topicValue);
        gradeRepository.save(gradeValue);
    }


}
