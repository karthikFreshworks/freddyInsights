package com.freshworks.freddy.insights.controller;

import com.freshworks.freddy.insights.aspect.AIAuthorization;
import com.freshworks.freddy.insights.aspect.AICustomAuthorization;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.CustomAccessTypeEnum;
import com.freshworks.freddy.insights.controller.v2.AIInsightControllerV2;
import com.freshworks.freddy.insights.controller.v2.AIPersistenceFilterController;
import com.freshworks.freddy.insights.dto.AIResponseDTO;
import com.freshworks.freddy.insights.dto.AIResponsePaginationDTO;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightFiltersDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightParamDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightResponseDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.service.AIInsightService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.freshworks.freddy.insights.constant.AIRequestConstant.ACCEPT_LANGUAGE;

@Slf4j
@RestController
@RequestMapping("/{bundle}/freddy")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AIFirstBundleController extends AbstractAIBaseHelper {
    private AIInsightController aiInsightController;
    private AIInsightControllerV2 insightControllerV2;
    private AIPersistenceFilterController aiPersistenceFilterController;
    private AIInsightService insightService;

    @GetMapping(value = "/v2/insight/filters")
    public ResponseEntity<AIInsightFiltersDTO> getV2InsightFilters(@PathVariable("bundle") String bundle) {
        return aiPersistenceFilterController.getInsightFilters();
    }

    @PutMapping(value = "/v2/insight/filters")
    public ResponseEntity<AIInsightFiltersDTO> getV2InsightFilters(
            @Valid @RequestBody AIInsightFiltersDTO aiInsightFiltersDTO) {
        return aiPersistenceFilterController.setInsightFilters(aiInsightFiltersDTO);
    }

    @GetMapping(value = "/v1/insight")
    public ResponseEntity<List<AIInsightResponseDTO>> getInsights(
            PaginationDTO paginationDTO, AIInsightParamDTO insightParamDTO,
            @RequestParam Map<String, String> queryHashParameters, @RequestHeader(value = ACCEPT_LANGUAGE,
            required = false) String acceptLanguage) {
        return aiInsightController.getInsights(paginationDTO, insightParamDTO, queryHashParameters, acceptLanguage);
    }

    @GetMapping(value = "/v2/insight")
    public ResponseEntity<AIResponsePaginationDTO<AIInsightResponseDTO>> getV2Insights(
            PaginationDTO paginationDTO, AIInsightParamDTO insightParamDTO,
            @RequestParam Map<String, String> queryHashParameters, @RequestHeader(value = ACCEPT_LANGUAGE,
            required = false) String acceptLanguage) {
        return insightControllerV2.getInsights(paginationDTO, insightParamDTO, queryHashParameters, acceptLanguage);
    }

    @DeleteMapping(value = "/v2/insight/{id}/dismiss")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIResponseDTO> dismissInsight(
            @PathVariable("id") String insightId) {
        log.info("Inside dismiss insight");
        AIResponseDTO response = insightService.dismissInsight(insightId);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/v2/insight/{id}/dismiss/undo")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIInsightEntity> undoInsight(
            @PathVariable("id") String insightId) {
        log.info("Inside undo insight");
        AIInsightEntity response = insightService.undoInsight(insightId);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
