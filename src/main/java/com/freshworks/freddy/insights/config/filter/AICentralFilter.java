package com.freshworks.freddy.insights.config.filter;

import com.freshworks.freddy.insights.constant.CentralConstant;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.dto.central.CentralRequestDTO;
import com.freshworks.freddy.insights.handler.impl.RequestControllerMappingHandlerImpl;
import com.freshworks.freddy.insights.helper.AICommonHelper;
import com.freshworks.freddy.insights.modelobject.central.AIDialogueCentralRequestPayload;
import com.freshworks.freddy.insights.modelobject.central.AIDialogueCentralResponsePayload;
import com.freshworks.freddy.insights.modelobject.central.AISprinklerCentralRequestPayload;
import com.freshworks.freddy.insights.modelobject.central.AISprinklerCentralResponsePayload;
import com.freshworks.freddy.insights.service.central.CentralProducerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(5)
@Slf4j
public class AICentralFilter extends OncePerRequestFilterWrapper {
    @Autowired
    private CentralProducerService centralProducerService;

    @Autowired
    private RequestControllerMappingHandlerImpl reqControllerMappingHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = reqControllerMappingHandler.getControllerMethod(httpServletRequest);
        boolean isDialogueRequest = isDialogueRequest(method);
        boolean isSprinklerRequest = isSprinklerRequest(method);

        BufferedRequestWrapper bufferedRequestWrapper = new BufferedRequestWrapper(httpServletRequest);

        // Send runService/completion request information to central
        if (isSprinklerRequest) {
            sendAiServiceRequestPayloadToCentral(bufferedRequestWrapper, method);
        }
        // send ai dialogue request information to central
        if (isDialogueRequest) {
            sendAiDialogueRequestPayloadToCentral(bufferedRequestWrapper);
        }

        BufferedResponseWrapper bufferedHttpServletResponse = new BufferedResponseWrapper(httpServletResponse);
        var startTime = Instant.now();
        MDC.put(ObservabilityConstant.START_TIME, AICommonHelper.convertToISODate(startTime));
        filterChain.doFilter(bufferedRequestWrapper, bufferedHttpServletResponse);
        var endTime = Instant.now();

        if (bufferedHttpServletResponse.getStatus() >= 400) {
            MDC.put(ObservabilityConstant.ERROR, bufferedHttpServletResponse.getContent());
        }
        MDC.put(ObservabilityConstant.END_TIME, AICommonHelper.convertToISODate(endTime));
        MDC.put(ObservabilityConstant.STATUS, String.valueOf(bufferedHttpServletResponse.getStatus()));
        MDC.put(ObservabilityConstant.METHOD, method);
        MDC.put(ObservabilityConstant.DURATION, String.valueOf(endTime.toEpochMilli()
                - startTime.toEpochMilli()));

        // Send runService/completion response information to central
        if (isSprinklerRequest) {
            sendAiServiceResponsePayloadToCentral(method);
        }

        // send ai dialogue response information to central
        if (isDialogueRequest) {
            sendAiDialogueResponsePayloadToCentral(bufferedHttpServletResponse);
        }
    }

    private void sendAiDialogueRequestPayloadToCentral(BufferedRequestWrapper bufferedRequestWrapper)
            throws IOException {
        var requestBody = bufferedRequestWrapper.getRequestBody();
        MDC.put(ObservabilityConstant.DIALOGUE_REQUEST, requestBody);
        AIDialogueCentralRequestPayload payload = new AIDialogueCentralRequestPayload();
        CentralRequestDTO<AIDialogueCentralRequestPayload> centralRequest;
        centralRequest = new CentralRequestDTO<>(CentralConstant.dialogue_trigger_request, payload);
        centralRequest.setPayloadVersion(CentralConstant.aiDialogue_payload_version);
        centralProducerService.sendEventsToCentral(centralRequest);
    }

    private void sendAiDialogueResponsePayloadToCentral(BufferedResponseWrapper bufferedResponseWrapper) {
        AIDialogueCentralResponsePayload payload = new AIDialogueCentralResponsePayload();
        payload.setResponse(bufferedResponseWrapper.getContent());
        CentralRequestDTO<AIDialogueCentralResponsePayload> centralRequest;
        centralRequest = new CentralRequestDTO<>(CentralConstant.dialogue_trigger_response, payload);
        centralRequest.setPayloadVersion(CentralConstant.aiDialogue_payload_version);
        centralProducerService.sendEventsToCentral(centralRequest);
    }

    private void sendAiServiceRequestPayloadToCentral(BufferedRequestWrapper bufferedRequestWrapper, String method)
            throws IOException {
        var requestBody = bufferedRequestWrapper.getRequestBody();
        MDC.put(ObservabilityConstant.LLM_REQUEST, requestBody);
        AISprinklerCentralRequestPayload payload = new AISprinklerCentralRequestPayload();

        CentralRequestDTO<AISprinklerCentralRequestPayload> centralRequest;
        if (ObservabilityConstant.AI_SERVICE_CONTROLLER_RUN.equalsIgnoreCase(method)) {
            centralRequest = new CentralRequestDTO<>(CentralConstant.sprinkler_run_request, payload);
        } else {
            centralRequest = new CentralRequestDTO<>(CentralConstant
                    .sprinkler_completion_request, payload);
        }
        centralRequest.setPayloadVersion(CentralConstant.sprinkler_payload_version);
        centralProducerService.sendEventsToCentral(centralRequest);
    }

    private void sendAiServiceResponsePayloadToCentral(String method) {
        AISprinklerCentralResponsePayload payload = new AISprinklerCentralResponsePayload();
        CentralRequestDTO<AISprinklerCentralResponsePayload> centralRequest;

        if (ObservabilityConstant.AI_SERVICE_CONTROLLER_RUN.equalsIgnoreCase(method)) {
            centralRequest = new CentralRequestDTO<>(CentralConstant.sprinkler_run_response, payload);
        } else {
            centralRequest = new CentralRequestDTO<>(CentralConstant
                    .sprinkler_completion_response, payload);
        }
        centralRequest.setPayloadVersion(CentralConstant.sprinkler_payload_version);
        centralProducerService.sendEventsToCentral(centralRequest);
    }
}
