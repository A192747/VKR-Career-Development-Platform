package org.example.mainservice.course.promotion;

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
import org.example.mainservice.course.promotion.service.PromotionMapper;
import org.example.mainservice.course.promotion.service.PromotionService;
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
@RequestMapping("/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;


    @Operation(summary = "Get promotion by id",
            description = "You can get promotion value by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public PromotionDTO getById(@PathVariable Long id) {
        return promotionMapper.toDTO(promotionService.findById(id));
    }


    @Operation(summary = "Get promotion by id",
            description = "You can get promotion value by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<PromotionDTO> getMyPromotion() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return promotionService.getPromotionByUserId(UUID.fromString(authentication.getName())).stream()
                .map(promotionMapper::toDTO)
                .toList();
    }



    @Operation(summary = "Get promotion by id",
            description = "You can get promotion value by using its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}")
    public List<PromotionDTO> getSomeUserPromotion(@PathVariable UUID id) {
        return promotionService.getPromotionByUserId(id).stream()
                .map(promotionMapper::toDTO)
                .toList();
    }



    @Operation(summary = "Get promotions page (list)",
            description = "You can get page of promotions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion page successfully got",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<PromotionDTO> getGradesPage(@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0) Integer page,
                                            @RequestParam(name = "limit", required = false, defaultValue = "5") @Min(value = 1) @Max(value = 100) Integer limit,
                                            @RequestParam(name = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection) {
        Sort sort = Sort.by(sortDirection, "id");
        return promotionService.getAllPromotions(page, limit, sort).map(promotionMapper::toDTO);
    }



    @Operation(summary = "Save new promotion",
            description = "You can save new promotion, and get it`s id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Long save(@RequestBody PromotionCreateDTO promotionDTO) throws BadRequestException {
        return promotionService.save(promotionMapper.toEntity(promotionDTO));
    }


    @Operation(summary = "Update promotion",
            description = "You can update promotion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void update(@RequestBody PromotionDTO promotionDTO) {
        promotionService.update(promotionMapper.toEntity(promotionDTO));
    }



    @Operation(summary = "Delete promotion",
            description = "You can delete promotion only if you are admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete success",
                    content = @Content(mediaType = "application/json")),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }

}
