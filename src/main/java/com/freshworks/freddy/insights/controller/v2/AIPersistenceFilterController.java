package com.freshworks.freddy.insights.controller.v2;

import com.freshworks.freddy.insights.aspect.AIAuthorization;
import com.freshworks.freddy.insights.aspect.AIBundleAuthorization;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.dto.insight.AIInsightFiltersDTO;
import com.freshworks.freddy.insights.helper.AbstractAIBaseApiHelper;
import com.freshworks.freddy.insights.service.AIInsightFilterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(AbstractAIBaseApiHelper.API_V2)
public class AIPersistenceFilterController {
    private final AIInsightFilterService aiInsightFilterService;

    @PutMapping(value = "/insight/filters")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN, AccessType.USER})
    @AIBundleAuthorization
    public ResponseEntity<AIInsightFiltersDTO> setInsightFilters(
            @Valid @RequestBody AIInsightFiltersDTO aiInsightFiltersDTO) {
        AIInsightFiltersDTO insightFiltersResponse = aiInsightFilterService.setAiInsightFilters(aiInsightFiltersDTO);
        return new ResponseEntity<>(insightFiltersResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/insight/filters")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN, AccessType.USER})
    @AIBundleAuthorization
    public ResponseEntity<AIInsightFiltersDTO> getInsightFilters() {
        AIInsightFiltersDTO insightFiltersResponse = aiInsightFilterService.getAiInsightFilters();
        return new ResponseEntity<>(insightFiltersResponse, HttpStatus.OK);
    }
}
