package com.freshworks.freddy.insights.handler.observability;

import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component(ObservabilityConstant.DIALOGUE_OBSERVABILITY)
public class AIDialogueObservabilityHandlerImpl extends AbstractObservabilityHandler {
    private final AIServiceRunObservabilityHandlerImpl aiServiceRunObservabilityHandler;

    @Autowired
    public AIDialogueObservabilityHandlerImpl(AIServiceRunObservabilityHandlerImpl aiServiceRunObservabilityHandler) {
        this.aiServiceRunObservabilityHandler = aiServiceRunObservabilityHandler;
    }

    @Override
    public StringBuilder getLogBuilder() {
        StringBuilder sb = aiServiceRunObservabilityHandler.getLogBuilder();
        sb.append(DELIMITER)
                .append(ObservabilityConstant.X_FW_DLG_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_DIALOGUE_ID, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_SMNTIC_CHE_HNDLR_STRT_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_SEMANTIC_CACHE_HANDLER_START_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_SMNTIC_CHE_HNDLR_END_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_SEMANTIC_CACHE_HANDLER_END_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_SMNTIC_CHE_HNDLR_RESP_DUR)
                .append(ASSIGNMENT)
                .append(getOrDefault(
                        ObservabilityConstant.DIALOGUE_SEMANTIC_CACHE_HANDLER_RESPONSE_DURATION, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_SMNTIC_CHE_HNDLR_STATUS_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_SEMANTIC_CACHE_HANDLER_STATUS_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_SMNTIC_CHE_HNDLR_ERR)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_SEMANTIC_CACHE_HANDLER_ERROR, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_HNDLR_TPE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_HANDLER_TYPE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_STRT_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_START_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_END_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_END_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ID, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_RESP_DUR)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_DURATION, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_RESP_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_RESPONSE_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_STATUS_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_STATUS_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_QRY)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_QUERY, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_INTNT_HNDLR_ERR)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_INTENT_HANDLER_ERROR, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.DLG_RES_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DIALOGUE_RESPONSE_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_COT_RES_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_COT_RESPONSE_CODE, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_COT_RES_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_COT_RESPONSE_TIME, EMPTY_STRING))
                .append(DELIMITER)
                .append(ObservabilityConstant.LLM_COT_ERR_MSG)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.LLM_COT_ERROR_MESSAGE, EMPTY_STRING));
        return sb;
    }

    @Override
    public void recordMetrics() {
        aiServiceRunObservabilityHandler.recordMetrics();
    }
}
