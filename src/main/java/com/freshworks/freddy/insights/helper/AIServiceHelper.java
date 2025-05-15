package com.freshworks.freddy.insights.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.service.AIServiceBaseDTO;
import com.freshworks.freddy.insights.valueobject.AIRunServiceVO;
import com.octomix.josson.Josson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Component
@AllArgsConstructor
public class AIServiceHelper extends AbstractAIBaseHelper {
    /**
     * Checks if the provided HTTP request is targeted towards an AI service URL based on certain criteria.
     * The criteria include matching the URL format and presence of specific headers.
     *
     * @param request the HttpServletRequest object representing the incoming HTTP request
     * @return true if the request is for an AI service URL, false otherwise
     */
    public static boolean isAiServiceURL(HttpServletRequest request) {
        String tenant = MDC.get("tenant");
        String platform = MDC.get("platform");

        String desiredUrlFormat = String.format("ai-service/%s/%s", tenant, platform);

        String uri = request.getRequestURI();
        String aiServiceVersion = request.getHeader("AI-Service-Version");
        String aiModel = request.getHeader("AI-model");

        return uri.contains(desiredUrlFormat)
                && (aiServiceVersion != null && !aiServiceVersion.isEmpty())
                && (aiModel != null && !aiModel.isEmpty());
    }

    public static JsonNode parseJson(Josson jossonJson, String parser) {
        JsonNode llmJsonNodeResponse;
        if ("-".equalsIgnoreCase(parser) || parser == null || "".equals(parser.trim())) {
            llmJsonNodeResponse = jossonJson.getNode();
        } else {
            llmJsonNodeResponse = jossonJson.getNode(parser);
        }
        return llmJsonNodeResponse;
    }

    public static <T> Object excludeFields(T object, String[] fieldsToRemove) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(object);
        // Traverse the JsonNode and remove the matching field
        for (String field : fieldsToRemove) {
            JsonNode fieldToRemove = jsonNode.get(field);
            if (fieldToRemove != null) {
                ((ObjectNode) jsonNode).remove(field);
            }
        }
        // Convert the modified JsonNode back to object
        try {
            return objectMapper.treeToValue(jsonNode, object.getClass());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPromptKey(boolean isCot) {
        return isCot ? ObservabilityConstant.LLM_COT_PROMPT : ObservabilityConstant.LLM_PROMPT;
    }

    public static String getResponseKey(boolean isCot) {
        return isCot ? ObservabilityConstant.LLM_COT_RESPONSE : ObservabilityConstant.LLM_RESPONSE;
    }

    public static String getResponseTimeKey(boolean isCot) {
        return isCot ? ObservabilityConstant.LLM_COT_RESPONSE_TIME : ObservabilityConstant.LLM_RESPONSE_TIME;
    }

    public static String getResponseCodeKey(boolean isCot) {
        return isCot ? ObservabilityConstant.LLM_COT_RESPONSE_CODE : ObservabilityConstant.LLM_RESPONSE_CODE;
    }

    public static String getErrorMessageKey(boolean isCot) {
        return isCot ? ObservabilityConstant.LLM_COT_ERROR_MESSAGE : ObservabilityConstant.LLM_ERROR_MESSAGE;
    }

    public String generateCurl(List<AIServiceBaseDTO.Param> paramList, @NotNull TenantEnum tenantEnum,
                               @NotNull String service, @NotNull String version,
                               @NotNull PlatformEnum platformEnum, @NotNull String model, ApiMethodEnum apiMethodEnum) {
        StringBuilder curlCommandBuilder;
        if (ApiMethodEnum.stream_post.equals(apiMethodEnum)) {
            curlCommandBuilder = getStreamCurlCommand(tenantEnum, platformEnum, service, version, model);
        } else {
            curlCommandBuilder = new StringBuilder("curl --location '")
                    .append(appConfigHelper.getPlatformHost())
                    .append("/v1/ai-service/")
                    .append(tenantEnum)
                    .append("/")
                    .append(platformEnum)
                    .append("/")
                    .append(service)
                    .append("'")
                    .append(" --header 'AI-Service-Version: ")
                    .append(version)
                    .append("'")
                    .append(" --header 'AI-Model: ")
                    .append(model)
                    .append("'")
                    .append(" --header 'Freddy-Ai-Platform-Authorization: {{auth_key}}'")
                    .append(" --header 'Authorization: Bearer {{token}}'");
        }
        switch (apiMethodEnum) {
        case multipart_post:
        case multipart_put:
            appendMultipartFormData(curlCommandBuilder, paramList);
            break;
        default:
            appendRequestData(curlCommandBuilder, "application/json", paramList);
            break;
        }
        return curlCommandBuilder.toString();
    }

    private StringBuilder getStreamCurlCommand(TenantEnum tenantEnum, PlatformEnum platformEnum,
                                               String service, String version, String model) {
        return new StringBuilder("curl --location '")
                .append(appConfigHelper.getPlatformHost())
                .append("/v1/ai-service/")
                .append(tenantEnum)
                .append("/")
                .append(platformEnum)
                .append("/")
                .append(service)
                .append("/")
                .append("stream")
                .append("'")
                .append(" --header 'AI-Service-Version: ")
                .append(version)
                .append("'")
                .append(" --header 'AI-Model: ")
                .append(model)
                .append("'")
                .append(" --header 'Freddy-Ai-Platform-Authorization: {{auth_key}}'")
                .append(" --header 'Authorization: Bearer {{token}}'");
    }

    private void appendRequestData(
            StringBuilder curlCommandBuilder, String contentType, List<AIServiceBaseDTO.Param> paramList) {
        curlCommandBuilder.append(" --header 'Content-Type: ").append(contentType).append("'");
        if (paramList != null && !paramList.isEmpty()) {
            String jsonData = new JSONObject(paramList.stream()
                    .filter(param -> param.getName() != null && !"".equals(param.getName().trim()))
                    .collect(Collectors.toMap(AIServiceBaseDTO.Param::getName,
                            AIServiceBaseDTO.Param::getDescription)))
                    .toString();
            curlCommandBuilder.append(" --data '")
                    .append(jsonData)
                    .append("'");
        } else {
            curlCommandBuilder.append(" --data '{\"exampleKey\": \"exampleValue\"}'");
        }
    }

    private void appendMultipartFormData(StringBuilder curlCommandBuilder, List<AIServiceBaseDTO.Param> paramList) {
        curlCommandBuilder.append(" --header 'Content-Type: multipart/form-data'");
        StringBuilder formDataBuilder = new StringBuilder();
        if (paramList == null || paramList.isEmpty()) {
            formDataBuilder.append(" -F 'exampleKey=exampleValue'");
        } else {
            paramList.stream()
                    .filter(param -> param.getName() != null && !param.getName().trim().isEmpty())
                    .forEach(param -> formDataBuilder.append(
                            String.format(" -F '%s=%s'", param.getName(), param.getDescription())));
        }
        curlCommandBuilder.append(formDataBuilder);
    }

    public String[] headerMapToArray(Map<String, String> headers) {
        List<String> headersArray = new ArrayList<>();

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                headersArray.add(header.getKey());
                headersArray.add(AIServiceHelper.getTemplate(
                        appConfigHelper.getFreddyAIPlatformLLMSecrets(), header.getValue(), "%(", ")"));
            }
        } else {
            headersArray.add("Content-Type");
            headersArray.add("application/json");
        }
        return headersArray.toArray(String[]::new);
    }

    public String[] headerMapObjectToArray(Map<String, Object> headers) {
        List<String> headersArray = new ArrayList<>();

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                headersArray.add(header.getKey());
                headersArray.add(AIServiceHelper.getTemplate(
                        appConfigHelper.getFreddyAIPlatformLLMSecrets(), header.getValue(), "%(", ")"));
            }
        } else {
            headersArray.add("Content-Type");
            headersArray.add("application/json");
        }
        return headersArray.toArray(String[]::new);
    }

    public String generateAIServiceId(AIRunServiceVO aiRunServiceVO) {
        return String.format("%s-%s-%s-%s-%s", aiRunServiceVO.getTenant().name(), aiRunServiceVO.getPlatform().name(),
                aiRunServiceVO.getAiModel(),
                aiRunServiceVO.getService(),
                aiRunServiceVO.getServiceVersion());
    }
}
