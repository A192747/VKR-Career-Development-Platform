package org.example.mainservice.llm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.TopicFacade;
import org.example.mainservice.llm.service.frontendDto.AnswerDTO;
import org.example.mainservice.llm.service.frontendDto.EvaluateDTO;
import org.example.mainservice.llm.service.frontendDto.QuestionDTO;
import org.example.mainservice.llm.service.frontendDto.TopicsDTO;
import org.example.mainservice.llm.service.llmDto.AnswerLLMDTO;
import org.example.mainservice.llm.service.llmDto.EvaluateLLMDTO;
import org.example.mainservice.llm.service.llmDto.QuestionLLMDTO;
import org.example.mainservice.llm.service.llmDto.TopicsLLMDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LLMServiceImpl implements LLMService {
    private final RestTemplate restTemplate;
    private final TopicFacade topicFacade;
    private final HashService hashService;
    @Value("${llm.uri}")
    private String serviceAddress;
    @Override
    public AnswerDTO getAnswer(QuestionDTO questionDTO) throws BadRequestException, NoSuchAlgorithmException {
        isQuestionDTOCorrect(questionDTO);

        QuestionLLMDTO questionLLMDTO = new QuestionLLMDTO();
        questionLLMDTO.setQuestion(questionDTO.getQuestion());

        AnswerLLMDTO answerLLMDTO = restTemplate.postForEntity(serviceAddress + "/answer", questionLLMDTO, AnswerLLMDTO.class)
                .getBody();

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setReferenceAnswer(Objects.requireNonNull(answerLLMDTO).getAnswer());
        answerDTO.setHash(hashService.hash(answerLLMDTO.getAnswer()));
        return answerDTO;
    }

    private void isQuestionDTOCorrect(QuestionDTO questionDTO) throws NoSuchAlgorithmException, BadRequestException {
        if(!hashService.hash(questionDTO.getQuestion()).equals(questionDTO.getHash()))
            throw new BadRequestException("Вы подменили вопрос!");
    }

    private void isEvaluateDTOCorrect(EvaluateDTO evaluateDTO) throws NoSuchAlgorithmException, BadRequestException {
        if(!hashService.hash(evaluateDTO.getAnswer().getReferenceAnswer()).equals(evaluateDTO.getAnswer().getHash()))
            throw new BadRequestException("Вы подменили ответ!");
    }

    @Override
    public List<QuestionDTO> getQuestions(TopicsDTO topicsDTO) throws NoSuchAlgorithmException {
        TopicsLLMDTO topicsLLMDTO = new TopicsLLMDTO();
        List<String> topics = topicsDTO.getTopicsIds().stream()
                .map(topicFacade::getTopicName)
                .toList();

        topicsLLMDTO.setTopics(topics);
        topicsLLMDTO.setNumQuestions(topicsDTO.getNumQuestions());


        ResponseEntity<List<AnswerLLMDTO>> responseEntity = restTemplate.exchange(
                serviceAddress + "/questions",
                HttpMethod.POST,
                new HttpEntity<>(topicsLLMDTO),
                new ParameterizedTypeReference<List<AnswerLLMDTO>>() {}
        );

        List<AnswerLLMDTO> list = responseEntity.getBody();

        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (AnswerLLMDTO str: Objects.requireNonNull(list)) {
            QuestionDTO questionDTO = new QuestionDTO();
            questionDTO.setQuestion(str.getAnswer());
            questionDTO.setHash(hashService.hash(str.getAnswer()));
            questionDTOList.add(questionDTO);
        }
        return questionDTOList;
    }

    @Override
    public String evaluate(EvaluateDTO evaluateDTO) throws BadRequestException, NoSuchAlgorithmException {
        EvaluateLLMDTO evaluateLLMDTO = new EvaluateLLMDTO();
        isEvaluateDTOCorrect(evaluateDTO);
        evaluateLLMDTO.setReferenceAnswer(evaluateDTO.getAnswer().getReferenceAnswer());
        evaluateLLMDTO.setUserAnswer(evaluateDTO.getUserAnswer());
        return restTemplate.postForEntity(serviceAddress + "/evaluate", evaluateLLMDTO, String.class).getBody();
    }
}
