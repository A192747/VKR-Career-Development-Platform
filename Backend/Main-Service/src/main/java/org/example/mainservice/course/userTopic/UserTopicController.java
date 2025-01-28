package org.example.mainservice.course.userTopic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.mainservice.course.promotion.service.PromotionCreateDTO;
import org.example.mainservice.course.promotion.service.PromotionDTO;
import org.example.mainservice.course.userTopic.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class UserTopicController {

    private final UserTopicService userTopicService;
    private final UserTopicMapper userTopicMapper;

    @Operation(summary = "Get progress by id",
            description = "You can get progress value by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserTopic successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserTopicDTO getById(@PathVariable Long id) {
        return userTopicMapper.toDTO(userTopicService.getById(id));
    }


    @Operation(summary = "Get my progress",
            description = "You can get progress value for you")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<UserTopicDTO> getMyPromotion() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userTopicService.getAllUserTopicByUserId(UUID.fromString(authentication.getName())).stream()
                .map(userTopicMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Update MY progress",
            description = "You can update YOUR progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/my")
    public void updateMyProgress(@RequestBody UserTopicUpdateMyDTO userTopicDTO) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userTopicService.updateMy(userTopicMapper.toEntity(userTopicDTO), UUID.fromString(authentication.getName()));
    }



    @Operation(summary = "Get progress by user id",
            description = "You can get progress value by using user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "progress successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}")
    public List<UserTopicDTO> getSomeUserTopics(@PathVariable UUID id) {
        return userTopicService.getAllUserTopicByUserId(id).stream()
                .map(userTopicMapper::toDTO)
                .toList();
    }



    @Operation(summary = "Get progress page (list)",
            description = "You can get page of progresses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserTopicDTO> getGradesPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                            @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                            @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, "id");
        return userTopicService.getAllUserTopic(page, limit, sort).map(userTopicMapper::toDTO);
    }



    @Operation(summary = "Save new progress",
            description = "You can save new progress, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Long save(@RequestBody UserTopicCreateDTO userTopicCreateDTO) throws BadRequestException {
        return userTopicService.save(userTopicMapper.toEntity(userTopicCreateDTO));
    }


    @Operation(summary = "Update progress",
            description = "You can update progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody UserTopicDTO userTopicDTO) throws BadRequestException {
        userTopicService.update(userTopicMapper.toEntity(userTopicDTO));
    }



    @Operation(summary = "Delete progress",
            description = "You can delete progress only if you are admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userTopicService.delete(id);
    }

}
