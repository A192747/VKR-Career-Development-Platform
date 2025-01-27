package org.example.mainservice.course.grade;

import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.mainservice.course.grade.service.GradeDTO;
import org.example.mainservice.course.grade.service.GradeMapper;
import org.example.mainservice.course.grade.service.GradeService;
import org.example.mainservice.course.topic.service.TopicDTO;
import org.example.mainservice.course.topic.service.TopicMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grade")
@RequiredArgsConstructor
public class GradeController {

    @Value("${sorting.field:id}")
    private String field;
    private final GradeService gradeService;
    private final GradeMapper gradeMapper;
    private final TopicMapper topicMapper;

    @Operation(summary = "Get grade by id",
            description = "You can get grade info by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public GradeDTO getById(@PathVariable @Min(value = 1) Long id) {
        return gradeMapper.toDTO(gradeService.getGradeById(id));
    }



    @Operation(summary = "Get topics list by id",
            description = "You can get list of topics from grade by using its grade id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topics successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/topics")
    public List<TopicDTO> getTopics(@PathVariable @Min(value = 1) Long id) {
        return gradeService.getTopicsForGrade(id).stream()
                .map(topicMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Save topic list in grade",
            description = "You can get save topic inside grade topicId request param")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topic successfully saved",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/topics")
    public void saveTopics(@PathVariable @Min(value = 1) Long id, @RequestParam Long topicId) {
        gradeService.saveTopic(id, topicId);
    }




    @Operation(summary = "Remove topic from grade",
            description = "You can delete topic from grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topic successfully removed",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/topics")
    public void deleteTopics(@PathVariable @Min(value = 1) Long id, @RequestParam Long topicId) {
        gradeService.deleteTopic(id, topicId);
    }



    @Operation(summary = "Get grade page (list)",
            description = "You can get page of grades.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<GradeDTO> getGradesPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                        @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                        @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, field);
        return gradeService.getAllGrades(page, limit, sort).map(gradeMapper::toDTO);
    }



    @Operation(summary = "Save grade",
            description = "You can save grade, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public long save(@RequestBody GradeDTO gradeDTO) {
        return gradeService.save(gradeMapper.toEntity(gradeDTO));
    }


    @Operation(summary = "Update grade",
            description = "You can update grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody GradeDTO gradeDTO) {
        gradeService.update(gradeMapper.toEntity(gradeDTO));
    }



    @Operation(summary = "Delete grade",
            description = "You can delete grade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Min(value = 1) Long id) {
        gradeService.delete(id);
    }

}
