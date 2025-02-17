package org.example.mainservice.llm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.coyote.BadRequestException;
import org.example.mainservice.llm.service.*;
import org.example.mainservice.llm.service.frontendDto.AnswerDTO;
import org.example.mainservice.llm.service.frontendDto.EvaluateDTO;
import org.example.mainservice.llm.service.frontendDto.QuestionDTO;
import org.example.mainservice.llm.service.frontendDto.TopicsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
@Slf4j
public class LLMController {
    private final LLMService llmService;

    @Operation(summary = "Get answer on question",
            description = "You should send this request during user answer writing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/answer")
    public AnswerDTO getAnswer(@RequestBody @Valid QuestionDTO questionDTO) throws BadRequestException, NoSuchAlgorithmException {
        return llmService.getAnswer(questionDTO);
    }


    @Operation(summary = "Get questions for list of topics",
            description = "This is a start params for interview. Num of questions for topic should be lower than 5")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/questions")
    public List<QuestionDTO> getQuestions(@RequestBody @Valid TopicsDTO topicsDTO) throws NoSuchAlgorithmException {
        return llmService.getQuestions(topicsDTO);
    }

    @Operation(summary = "Evaluate user answer",
            description = "You should send this request after getting user answer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evaluate successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/evaluate")
    public String evaluate(@RequestBody @Valid EvaluateDTO evaluateDTO) throws BadRequestException, NoSuchAlgorithmException {
        return llmService.evaluate(evaluateDTO);
    }



}
