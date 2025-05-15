package com.freshworks.freddy.insights.controller.v2;

import com.freshworks.freddy.insights.aspect.AIAuthorization;
import com.freshworks.freddy.insights.aspect.AIBundleAuthorization;
import com.freshworks.freddy.insights.aspect.AICustomAuthorization;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.CustomAccessTypeEnum;
import com.freshworks.freddy.insights.dto.AIResponsePaginationDTO;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightParamDTO;
import com.freshworks.freddy.insights.dto.insight.AIInsightResponseDTO;
import com.freshworks.freddy.insights.helper.AbstractAIBaseApiHelper;
import com.freshworks.freddy.insights.service.AIInsightService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController(AIInsightConstant.INSIGHT_CONTROLLER_V2)
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(AbstractAIBaseApiHelper.API_V2)
public class AIInsightControllerV2 {
    private AIInsightService insightService;

    @GetMapping(value = "/insight")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN, AccessType.USER})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    @AIBundleAuthorization
    public ResponseEntity<AIResponsePaginationDTO<AIInsightResponseDTO>> getInsights(
            @Valid PaginationDTO paginationDTO,
            @Valid AIInsightParamDTO insightParamDTO,
            @RequestParam Map<String, String> queryHashParameters,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        var insights = insightService.getInsights(insightParamDTO,
                paginationDTO, acceptLanguage, queryHashParameters);
        return new ResponseEntity<>(insights, HttpStatus.OK);
    }
}
