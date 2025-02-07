package org.example.mainservice.course.topic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mainservice.course.topic.service.TopicCreateDTO;
import org.example.mainservice.course.topic.service.TopicDTO;
import org.example.mainservice.course.topic.service.TopicMapper;
import org.example.mainservice.course.topic.service.TopicService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    @Value("${sorting.field:id}")
    private String field;
    private final TopicService topicService;
    private final TopicMapper topicMapper;

    @Operation(summary = "Get topic by id",
            description = "You can get topic info by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "topic successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public TopicDTO getById(@PathVariable @Min(value = 1) Long id) {
        return topicMapper.toDTO(topicService.findById(id));
    }


    @Operation(summary = "Get topic page (list)",
            description = "You can get page of topics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<TopicDTO> getTopicsPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                        @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                        @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, field);
        return topicService.getAllTopics(page, limit, sort).map(topicMapper::toDTO);
    }



    @Operation(summary = "Save topic",
            description = "You can save topic, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public long save(@RequestBody TopicCreateDTO topicDTO) {
        log.info("Topic value {}", topicDTO);
        return topicService.save(topicMapper.toEntity(topicDTO));
    }


    @Operation(summary = "Update topic",
            description = "You can update topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody TopicDTO TopicDTO) {
        topicService.update(topicMapper.toEntity(TopicDTO));
    }



    @Operation(summary = "Delete topic",
            description = "You can delete topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void update(@PathVariable @Min(value = 1) Long id) {
        topicService.delete(id);
    }

}
