package com.freshworks.freddy.insights.controller;

import com.freshworks.freddy.insights.aspect.AIAuthorization;
import com.freshworks.freddy.insights.aspect.AIBundleAuthorization;
import com.freshworks.freddy.insights.aspect.AICustomAuthorization;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.AccessType;
import com.freshworks.freddy.insights.constant.enums.CustomAccessTypeEnum;
import com.freshworks.freddy.insights.constant.enums.StatusEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.AIBulkResponseDTO;
import com.freshworks.freddy.insights.dto.AIResponseDTO;
import com.freshworks.freddy.insights.dto.PaginationDTO;
import com.freshworks.freddy.insights.dto.insight.*;
import com.freshworks.freddy.insights.dto.promotion.AIPromoteDTO;
import com.freshworks.freddy.insights.entity.AIInsightEntity;
import com.freshworks.freddy.insights.helper.AbstractAIBaseApiHelper;
import com.freshworks.freddy.insights.service.AIInsightService;
import com.freshworks.freddy.insights.validator.AllowedLanguageCodes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.freshworks.freddy.insights.constant.AIInsightConstant.*;
import static com.freshworks.freddy.insights.constant.AIRequestConstant.ACCEPT_LANGUAGE;

@Slf4j
@Validated
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AIInsightController extends AbstractAIBaseApiHelper {
    private AIInsightService insightService;

    @PostMapping(value = "/insight")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIResponseDTO> createInsight(
            @RequestBody @Valid AIInsightCreateDTO createDTO) {
        var responseDTO = insightService.createInsight(createDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping(value = "/insight/bulk",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIBulkResponseDTO> createBulkInsight(
            @RequestBody @Valid @NotEmpty @Size(max = 20) List<AIInsightCreateDTO> bulkCreateDTO) throws IOException {
        var responseDTO = insightService.createBulkInsight(bulkCreateDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.MULTI_STATUS);
    }

    @GetMapping(value = "/insight")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN, AccessType.USER})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    @AIBundleAuthorization
    public ResponseEntity<List<AIInsightResponseDTO>> getInsights(
            @Valid PaginationDTO paginationDTO,
            @Valid AIInsightParamDTO insightParamDTO,
            @RequestParam(required = false) Map<String, String> queryHashParameters,
            @RequestHeader(value = ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        var insights =
                insightService.getInsights(insightParamDTO, paginationDTO, acceptLanguage, queryHashParameters);
        return new ResponseEntity<>(insights.getRecords(), HttpStatus.OK);
    }

    @GetMapping(value = "/insight/{id}")
    @AIAuthorization({AccessType.SUPER_ADMIN, AccessType.ADMIN, AccessType.USER})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIInsightEntity> getInsightById(@PathVariable("id") String id) {
        var insightEntity = insightService.getInsightById(id);
        return new ResponseEntity<>(insightEntity, HttpStatus.OK);
    }

    @PutMapping(value = "/insight/{id}")
    @AIAuthorization({AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIResponseDTO> updateInsight(@PathVariable("id") String insightId,
                                                       @RequestBody @Valid AIInsightUpdateDTO updateDTO) {
        var responseDTO = insightService.updateInsight(insightId, updateDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PutMapping(value = "/insight/{id}/translation/{language_code}")
    @AIAuthorization({AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIResponseDTO> updateInsightTranslatedField(
            @PathVariable("id") String insightId,
            @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
            @PathVariable(value = LANGUAGE_CODE) String languageCode,
            @RequestBody @Valid AIInsightTranslationTextDTO requestDTO) {
        AIInsightTranslationDTO aiInsightTranslationDTO = AIInsightTranslationDTO.builder()
                .title(requestDTO.getTitle()).languageCode(languageCode).build();
        var responseDTO = insightService.updateInsightTranslatedField(insightId, aiInsightTranslationDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PatchMapping(value = "/insight/{id}")
    @AIAuthorization({AccessType.SUPER_ADMIN})
    public ResponseEntity<AIResponseDTO> updateInsightStatus(@PathVariable("id") String insightId,
                                                             @RequestParam(value = STATUS) StatusEnum status) {
        var responseDTO = insightService.updateInsightStatus(insightId, status);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @Deprecated
    @PostMapping(value = "/insight/promote")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    public ResponseEntity<Void> promoteService(@Valid @RequestBody List<AIPromoteDTO> promoteDTOs) {
        insightService.promoteAsync(promoteDTOs);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Deprecated
    @PutMapping(value = "/insight/promote")
    @AIAuthorization(value = {AccessType.SUPER_ADMIN, AccessType.ADMIN})
    public ResponseEntity<Void> promoteUpdateService(@Valid @RequestBody List<AIPromoteDTO> promoteDTOs) {
        insightService.promoteUpdateAsync(promoteDTOs);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @DeleteMapping(value = "/insight/{id}")
    @AIAuthorization({AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<Void> deleteInsight(@PathVariable("id") String id) {
        insightService.deleteInsight(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/insight/{id}/translation/{language_code}")
    @AIAuthorization({AccessType.SUPER_ADMIN, AccessType.ADMIN})
    @AICustomAuthorization(value = {CustomAccessTypeEnum.NEO_ANALYTICS})
    public ResponseEntity<AIResponseDTO> deleteTranslatedField(
            @PathVariable("id") String insightId,
            @AllowedLanguageCodes(message = ExceptionConstant.NOT_A_VALID_LANGUAGE_CODE)
            @PathVariable(value = LANGUAGE_CODE) String languageCode) {
        var responseDTO = insightService.deleteInsightTranslatedField(insightId, languageCode);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/insight")
    @AIAuthorization({AccessType.SUPER_ADMIN})
    public ResponseEntity<Void> deleteInsightsByTenant(
            @RequestParam(value = TENANT) TenantEnum tenant,
            @RequestParam(value = PROMPT_IDS, required = false) List<String> promptIds) {
        insightService.deleteInsightsByTenant(tenant, promptIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
