package org.example.mainservice.course.schedule;

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
import org.example.mainservice.course.schedule.service.ScheduleCreateDTO;
import org.example.mainservice.course.schedule.service.ScheduleDTO;
import org.example.mainservice.course.schedule.service.ScheduleMapper;
import org.example.mainservice.course.schedule.service.ScheduleService;
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
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    @Operation(summary = "Get schedule by id",
            description = "You can get schedule info by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "feedback successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ScheduleDTO getById(@PathVariable @Min(value = 1) Long id) {
        return scheduleMapper.toDTO(scheduleService.findById(id));
    }


    @Operation(summary = "Get schedule page (list)",
            description = "You can get page of schedules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<ScheduleDTO> geSchedulesPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                             @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                             @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, "createdAt");
        return scheduleService.getAllSchedules(page, limit, sort).map(scheduleMapper::toDTO);
    }


    @Operation(summary = "Get MINE Schedule page (list)",
            description = "You can get page of feedbacks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<ScheduleDTO> getMySchedulesPage() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return scheduleService.getAllScheduleByUserId(UUID.fromString(authentication.getName()))
                .stream()
                .map(scheduleMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Approve schedule event",
            description = "You can approve schedule event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/approve")
    public void approve(@RequestParam(name = "scheduleId") @Min(value = 0) Long id,
                        @RequestParam(name = "approved") Boolean approved) throws JsonProcessingException, BadRequestException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        scheduleService.approve(id, UUID.fromString(authentication.getName()), approved);
    }


    @Operation(summary = "Save Schedule",
            description = "You can save feedback, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public long save(@RequestBody ScheduleCreateDTO topicDTO) throws BadRequestException, JsonProcessingException {
        return scheduleService.save(scheduleMapper.toEntity(topicDTO));
    }


    @Operation(summary = "Update MINE Schedule",
            description = "You can update YOUR Schedule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @PutMapping
    public void update(@RequestBody ScheduleDTO scheduleDTO) throws BadRequestException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID myId = UUID.fromString(authentication.getName());
        if (scheduleDTO.getReviewedUserProfileId().equals(myId) ||
                scheduleDTO.getReviewerUserProfileId().equals(myId)) {
            scheduleService.update(scheduleMapper.toEntity(scheduleDTO));
        } else {
            throw new BadRequestException("Вы не можете редактировать расписание, которое к вам не относится");
        }
    }

    @Operation(summary = "Update ANYONE Schedule",
            description = "You can update ANYONE Schedule (ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public void updateAdmin(@RequestBody ScheduleDTO scheduleDTO) throws BadRequestException {
        scheduleService.update(scheduleMapper.toEntity(scheduleDTO));
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
        scheduleService.delete(id);
    }

}
