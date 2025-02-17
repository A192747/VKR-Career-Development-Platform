package org.example.mainservice.llm.service;

import org.apache.coyote.BadRequestException;
import org.example.mainservice.llm.service.frontendDto.AnswerDTO;
import org.example.mainservice.llm.service.frontendDto.EvaluateDTO;
import org.example.mainservice.llm.service.frontendDto.QuestionDTO;
import org.example.mainservice.llm.service.frontendDto.TopicsDTO;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface LLMService {
    AnswerDTO getAnswer(QuestionDTO questionDTO) throws BadRequestException, NoSuchAlgorithmException;
    List<QuestionDTO> getQuestions(TopicsDTO topicsDTO) throws NoSuchAlgorithmException;
    String evaluate(EvaluateDTO evaluateDTO) throws BadRequestException, NoSuchAlgorithmException;
}
