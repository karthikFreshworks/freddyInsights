package com.freshworks.freddy.insights.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.ApiMethodEnum;
import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AICommonHelper {
    /**
     * Resolves placeholders in a given template map using replacements provided in a replace map.
     *
     * @param <R>                      The type of replacements in the replace map
     * @param templateMap              The template map containing placeholders to be replaced
     * @param replaceMap               The map containing replacement values for the placeholders
     * @param prefix                   The prefix used to identify placeholders
     * @param suffix                   The suffix used to identify placeholders
     * @param retainUnusedPlaceholders Flag indicating whether to retain placeholders without replacements
     * @return The resolved map with placeholders replaced by their corresponding values
     */
    @SuppressWarnings("unchecked")
    public static <R> Map<String, Object> resolvePlaceholders(
            Map<String, Object> templateMap, Map<String, R> replaceMap, String prefix, String suffix,
            boolean retainUnusedPlaceholders) {
        Map<String, Object> resolvedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : templateMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String && isPlaceholderString((String) value, prefix, suffix)) {
                String placeholder = (String) value;
                R replacement = getReplacement(placeholder, replaceMap, prefix, suffix);
                if (replacement != null) {
                    resolvedMap.put(key, replacement);
                } else if (retainUnusedPlaceholders) {
                    resolvedMap.put(key, value);
                }
            } else if (value instanceof Map) {
                resolvedMap.put(key, resolvePlaceholders((Map<String, Object>) value, replaceMap, prefix, suffix,
                        retainUnusedPlaceholders));
            } else if (value instanceof List) {
                resolvedMap.put(key, resolveListPlaceholders((List<Object>) value, replaceMap, prefix, suffix,
                        retainUnusedPlaceholders));
            } else {
                resolvedMap.put(key, value);
            }
        }

        return resolvedMap;
    }

    /**
     * Resolves placeholders in a list of objects.
     *
     * @param <R>                      The type of replacements in the replace map
     * @param listValue                The list containing objects with placeholders to be replaced
     * @param replaceMap               The map containing replacement values for the placeholders
     * @param prefix                   The prefix used to identify placeholders
     * @param suffix                   The suffix used to identify placeholders
     * @param retainUnusedPlaceholders Flag indicating whether to retain placeholders without replacements
     * @return The resolved list with placeholders replaced by their corresponding values
     */
    @SuppressWarnings("unchecked")
    private static <R> List<Object> resolveListPlaceholders(
            List<Object> listValue, Map<String, R> replaceMap, String prefix, String suffix,
            boolean retainUnusedPlaceholders) {
        List<Object> resolvedList = new ArrayList<>();
        for (Object listItem : listValue) {
            if (listItem instanceof String && isPlaceholderString((String) listItem, prefix, suffix)) {
                String placeholder = (String) listItem;
                R replacement = getReplacement(placeholder, replaceMap, prefix, suffix);
                if (replacement != null) {
                    resolvedList.add(replacement);
                } else if (retainUnusedPlaceholders) {
                    resolvedList.add(listItem);
                }
            } else if (listItem instanceof Map) {
                resolvedList.add(resolvePlaceholders((Map<String, Object>) listItem, replaceMap, prefix, suffix,
                        retainUnusedPlaceholders));
            } else if (listItem instanceof List) {
                resolvedList.add(resolveListPlaceholders((List<Object>) listItem, replaceMap, prefix, suffix,
                        retainUnusedPlaceholders));
            } else {
                resolvedList.add(listItem);
            }
        }
        return resolvedList;
    }

    /**
     * Checks if the given string is a placeholder.
     * A placeholder is identified by starting with the specified prefix and ending with the specified suffix.
     *
     * @param str    The string to check
     * @param prefix The prefix used to identify placeholders
     * @param suffix The suffix used to identify placeholders
     * @return True if the string is a placeholder, false otherwise
     */
    private static boolean isPlaceholderString(String str, String prefix, String suffix) {
        return str.startsWith(prefix) && str.endsWith(suffix);
    }

    /**
     * Retrieves the replacement value for the given placeholder from the replaceMap.
     *
     * @param <R>        The type of replacements in the replace map
     * @param str        The placeholder string
     * @param replaceMap The map containing replacement values
     * @param prefix     The prefix used to identify placeholders
     * @param suffix     The suffix used to identify placeholders
     * @return The replacement value if found, null otherwise
     */
    private static <R> R getReplacement(String str, Map<String, R> replaceMap, String prefix, String suffix) {
        String placeholderKey = str.substring(prefix.length(), str.length() - suffix.length());
        return replaceMap.get(placeholderKey);
    }

    public static String headerToJsonString(Map<String, String> headers) {
        if (headers != null) {
            return new JSONObject(headers).toString();
        }
        return null;
    }

    public static Map<String, String> jsonStringToMap(String input) {
        if (StringUtils.isBlank(input)) {
            return new HashMap<>();
        }
        try {
            return new ObjectMapper().readValue(input, Map.class);
        } catch (Exception e) {
            throw new AIResponseStatusException(String.format("Error: unable to convert header json to map %s",
                    e.getMessage()));
        }
    }

    public static String convertToISODate(Instant dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneId.systemDefault());
        return formatter.format(dateTime);
    }

    public static AIResponseStatusException responseException(String responseBody, int statusCode) {
        ErrorCode errorCode;
        switch (statusCode) {
        case 400:
            errorCode = ErrorCode.EXTERNAL_SERVICE_BAD_REQUEST;
            break;
        case 404:
            errorCode = ErrorCode.EXTERNAL_SERVICE_RESOURCE_NOT_FOUND;
            break;
        case 500:
            errorCode = ErrorCode.EXTERNAL_SERVICE_INTERNAL_SERVER_ERROR;
            break;
        case 424:
            errorCode = ErrorCode.EXTERNAL_SERVICE_PRODUCT_CALL_FAILED;
            break;
        case 408:
            errorCode = ErrorCode.EXTERNAL_SERVICE_HTTP_CLIENT_TIMEOUT;
            break;
        case 501:
        case 502:
        case 503:
            errorCode = ErrorCode.EXTERNAL_SERVICE_SERVER_NOT_REACHABLE;
            break;
        case 504:
            errorCode = ErrorCode.EXTERNAL_SERVICE_GATEWAY_TIMEOUT_VALUE;
            break;
        default:
            errorCode = ErrorCode.HTTP_MESSAGE_NOT_WRITEABLE;
        }

        return new AIResponseStatusException(responseBody, HttpStatus.valueOf(statusCode), errorCode);
    }

    public static String getCustomHttpException(String url, ApiMethodEnum method, String message) {
        return String.format("%s, %s: %s, body: %s",
                ExceptionConstant.EXTERNAL_SERVICE_API_ERROR, method.name(), url, message);
    }
}
