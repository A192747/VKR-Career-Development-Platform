package org.example.mainservice.course.feedback;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.feedback.service.FeedbackCreateDTO;
import org.example.mainservice.course.feedback.service.FeedbackDTO;
import org.example.mainservice.course.feedback.service.FeedbackMapper;
import org.example.mainservice.course.feedback.service.FeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackMapper feedbackMapper;

    @Operation(summary = "Get feedback by id",
            description = "You can get feedback info by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "feedback successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public FeedbackDTO getById(@PathVariable @Min(value = 1) Long id) {
        return feedbackMapper.toDTO(feedbackService.findById(id));
    }


    @Operation(summary = "Get feedback page (list)",
            description = "You can get page of feedbacks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<FeedbackDTO> getFeedbacksPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                              @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                              @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, "createdAt");
        return feedbackService.getAllFeedbacks(page, limit, sort).map(feedbackMapper::toDTO);
    }


    @Operation(summary = "Get MINE feedback page (list)",
            description = "You can get page of feedbacks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<FeedbackDTO> getMyFeedbacksPage() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return feedbackService.getAllFeedbacksByUserId(UUID.fromString(authentication.getName()))
                .stream()
                .map(feedbackMapper::toDTO)
                .toList();
    }


    @Operation(summary = "Save feedback",
            description = "You can save feedback, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public long save(@RequestBody FeedbackCreateDTO topicDTO) throws BadRequestException, JsonProcessingException {
        log.info("Feedback value {}", topicDTO);
        return feedbackService.save(feedbackMapper.toEntity(topicDTO));
    }


    @Operation(summary = "Update feedback",
            description = "You can update feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody FeedbackDTO FeedbackDTO) {
        feedbackService.update(feedbackMapper.toEntity(FeedbackDTO));
    }


    @Operation(summary = "Delete feedback",
            description = "You can delete feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Min(value = 1) Long id) {
        feedbackService.delete(id);
    }

}
