package com.freshworks.freddy.insights.handler.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.freshworks.freddy.insights.constant.AIHandlerConstant;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.dto.service.AIStreamResponseFormatDTO;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.handler.AbstractAIHandler;
import com.freshworks.freddy.insights.handler.http.response.CustomHttpResponse;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import com.octomix.josson.Josson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component(AIHandlerConstant.AI_SERVICE_STREAM_STRATEGY)
public class ServiceStreamHandlerImpl extends AbstractAIHandler<AIServiceMO, AIServiceMO> {
    private static final String DONE_STATUS = "[DONE]";
    private static final String STOP = "stop";
    private static final String PREFIX_STRING = "data: ";

    public AIServiceMO executeStrategy(AIServiceMO aiServiceMO) throws AIResponseStatusException {
        try {
            CompletableFuture.runAsync(() ->  {
                var response = super.streamRequestHandler.get(
                        AIHandlerConstant.APACHE_STREAM_REQUEST).execute(aiServiceMO, aiServiceMO.getMethod());
                if (aiServiceMO.getPlatform().equals(PlatformEnum.azure)) {
                    handleAzureStreamResponse(response, aiServiceMO);
                }
            });
        } catch (Exception ex) {
            log.error("There was error executing run-service with stream. CAUSE: {}", ExceptionHelper.stackTrace(ex));
            aiServiceMO.getSseEmitter().completeWithError(ex);
        }
        return null;
    }

    private void handleAzureStreamResponse(CustomHttpResponse<InputStream> response, AIServiceMO aiServiceMO) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody()))) {
            reader.lines()
                    .filter(line -> line.startsWith(PREFIX_STRING))
                    .map(line -> line.substring(PREFIX_STRING.length()))
                    .forEach(line -> streamDataToSseEmitter(line, aiServiceMO));
            aiServiceMO.getSseEmitter().complete();
        } catch (AIResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while reading the stream response", ex);
            sendErrorMessage(aiServiceMO.getSseEmitter(), "Exception occurred while reading the stream response");
        }
    }

    private void streamDataToSseEmitter(String line, AIServiceMO aiServiceMO) throws AIResponseStatusException {
        var emitter = aiServiceMO.getSseEmitter();
        try {
            if (DONE_STATUS.equals(line)) {
                log.info("Stream response completed successfully");
                var responseStream = AIStreamResponseFormatDTO.builder().streamCompleted(true).build();
                emitter.send(SseEmitter.event().data(responseStream));
                return;
            }
            JsonNode jsonNode = objectMapper.readTree(line);
            String finishReason = Josson.from(jsonNode).getNode("choices[0].finish_reason").asText();
            if (STOP.equals(finishReason)) {
                log.info("Stop response from stream encountered");
                return;
            }
            JsonNode responseNode = getContentNode(aiServiceMO.getResponseParser(), jsonNode);
            if (responseNode != null && !responseNode.isMissingNode()) {
                var responseStream = AIStreamResponseFormatDTO.builder().content(responseNode).build();
                emitter.send(SseEmitter.event().data(responseStream));
            }
        } catch (Exception ex) {
            log.error("Exception occurred while parsing the data as response format", ex);
            sendErrorMessage(emitter, "Exception occurred while parsing the data as response format");
        }
    }

    private JsonNode getContentNode(String responseParser, JsonNode jsonNode) {
        if (responseParser != null && !responseParser.equals("-")) {
            return Josson.from(jsonNode).getNode(responseParser);
        }
        return Josson.from(jsonNode).getNode();
    }

    private void sendErrorMessage(SseEmitter emitter, String errorMessage) {
        try {
            var errorResponse = AIStreamResponseFormatDTO.builder().errorMessage(errorMessage).build();
            emitter.send(SseEmitter.event().data(errorResponse));
        } catch (IOException e) {
            log.error("Failed to send error message Exception : {}, Stacktrace : {}", e.getMessage(),
                    Arrays.toString(e.getStackTrace()));
        } finally {
            emitter.complete();
        }
    }
}
