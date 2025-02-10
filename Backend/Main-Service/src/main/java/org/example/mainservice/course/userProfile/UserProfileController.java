package org.example.mainservice.course.userProfile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.mainservice.course.userProfile.service.UserProfileCreateDTO;
import org.example.mainservice.course.userProfile.service.UserProfileDTO;
import org.example.mainservice.course.userProfile.service.UserProfileMapper;
import org.example.mainservice.course.userProfile.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    @Operation(summary = "Get my profile info",
            description = "You can get profile info (for current user)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserId successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public UserProfileDTO getMe() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userProfileMapper.toDTO(
                userProfileService.findById(UUID.fromString(authentication.getName()))
        );
    }


    @Operation(summary = "Get user by userId",
            description = "You can get user info by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserProfileDTO getById(@PathVariable UUID id) {
        return userProfileMapper.toDTO(userProfileService.findById(id));
    }


    @Operation(summary = "Set new grade for user",
            description = "You can set new grade for user only if you are an admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade successfully set",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/grade")
    public void setNewGrade(@PathVariable UUID id, @RequestParam @Min(value = 1) Long gradeId) {
        userProfileService.setNewGrade(id, gradeId);
    }


    @Operation(summary = "Get users page (list)",
            description = "You can get page of users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<UserProfileDTO> getGradesPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                              @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                              @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, "lastName");
        return userProfileService.getAllUserProfiles(page, limit, sort).map(userProfileMapper::toDTO);
    }


    @Operation(summary = "Save user",
            description = "You can save user, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UUID save(@RequestBody UserProfileCreateDTO userProfileDTO) {
        return userProfileService.save(
                userProfileMapper.toEntity(userProfileDTO),
                userProfileDTO.getGradeId()
        );
    }


    @Operation(summary = "Update user",
            description = "You can update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody UserProfileDTO userProfileDTO) {
        userProfileService.update(userProfileMapper.toEntity(userProfileDTO));
    }


    @Operation(summary = "Delete user",
            description = "You can delete user only if you are admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        userProfileService.delete(id);
    }

}
