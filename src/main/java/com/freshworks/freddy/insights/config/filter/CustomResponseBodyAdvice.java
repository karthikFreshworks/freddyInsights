package com.freshworks.freddy.insights.config.filter;

import com.freshworks.freddy.insights.constant.AIRequestConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.handler.ratelimit.UpdateTokenUsageHandlerImpl;
import com.freshworks.freddy.insights.modelobject.mdc.DialogueChronologyMDC;
import com.freshworks.freddy.insights.modelobject.mdc.SprinklerChronologyMDC;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final UpdateTokenUsageHandlerImpl updateTokenUsageHandler;

    @Autowired
    public CustomResponseBodyAdvice(UpdateTokenUsageHandlerImpl updateTokenUsageHandler) {
        this.updateTokenUsageHandler = updateTokenUsageHandler;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().add(AIRequestConstant.X_FW_REMAINING_TOKEN,
                updateTokenUsageHandler.calculateRemainingTokens());

        if (isDialogueChronologyHeadersNeeded()) {
            String dialogueChronologyMDC = new DialogueChronologyMDC().toJson();
            if (dialogueChronologyMDC != null && !"{}".equals(dialogueChronologyMDC)) {
                response.getHeaders().add(
                        ObservabilityConstant.X_FW_DIALOGUE_CHRONOLOGY, dialogueChronologyMDC);
            }
        }

        if (isSprinklerChronologyHeadersNeeded()) {
            String sprinklerChronologyMDC = new SprinklerChronologyMDC().toJson();
            if (sprinklerChronologyMDC != null && !"{}".equals(sprinklerChronologyMDC)) {
                response.getHeaders().add(
                        ObservabilityConstant.X_FW_SPRINKLER_CHRONOLOGY, sprinklerChronologyMDC);
            }
        }
        return body;
    }

    private boolean isSprinklerChronologyHeadersNeeded() {
        return isDialogueChronologyHeadersNeeded()
                || (MDC.get(ObservabilityConstant.LLM_COT_RESPONSE_CODE) != null
                || MDC.get(ObservabilityConstant.LLM_RESPONSE_CODE) != null);
    }

    private boolean isDialogueChronologyHeadersNeeded() {
        return MDC.get(ObservabilityConstant.X_FW_DIALOGUE_ID) != null;
    }
}
