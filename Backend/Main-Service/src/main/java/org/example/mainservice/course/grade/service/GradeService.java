package org.example.mainservice.course.grade.service;

import org.example.mainservice.course.grade.service.internal.Grade;
import org.example.mainservice.course.topic.service.internal.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface GradeService {
    long save(Grade grade);
    void update(Grade grade);
    void delete(long id);
    Grade getGradeById(long id);

    Page<Grade> getAllGrades(int page, int size, Sort sort);
    List<Topic> getTopicsForGrade(long id);
    void saveTopic(long gradeId, long topicId);
    void deleteTopic(long gradeId, long topicId);

}
