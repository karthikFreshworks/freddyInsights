package com.freshworks.freddy.insights.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Helper class for ObjectMapper operations.
 */
@Slf4j
@Component
public class ObjectMapperHelper {
    /**
     * The object mapper used for JSON serialization and deserialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Converts the given object to JSON string.
     *
     * @param <T>              The type of the object to convert to JSON.
     * @param object           The object to convert to JSON.
     * @param includeNulls     Whether to include null values in the JSON output.
     * @param logErrorTemplate The template for the log message.
     * @return JSON string representing the object.
     */
    public static <T> String toJson(T object, boolean includeNulls, String logErrorTemplate) {
        try {
            if (!includeNulls) {
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
            return objectToJsonString(object);
        } catch (JsonProcessingException e) {
            log.error(String.format("%s %s", logErrorTemplate, "to JSON: {}, CAUSE: {}"), e.getMessage(),
                    ExceptionHelper.stackTrace(e));
            return null;
        } finally {
            // Reset the serialization inclusion to default
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        }
    }

    /**
     * Reads JSON string and converts it into a {@code LinkedHashMap} of strings.
     *
     * @param json The JSON string to read.
     * @return A {@code LinkedHashMap} containing key-value pairs from the JSON string.
     * @throws Exception If there is an error while parsing the JSON.
     */
    public static LinkedHashMap<String, Object> readMapOfStrings(String json) throws Exception {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, Object.class);
        return objectMapper.readValue(json, mapType);
    }

    /**
     * Reads JSON string and converts it into a {@code LinkedList} of {@code Map}s.
     *
     * @param json The JSON string to read.
     * @return A {@code LinkedList} containing {@code Map}s parsed from the JSON string.
     * @throws Exception If there is an error while parsing the JSON.
     */
    public static LinkedList<Map<String, Object>> readLinkedListOfMaps(String json) throws Exception {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(LinkedList.class, Map.class);
        return objectMapper.readValue(json, collectionType);
    }

    /**
     * Reads JSON string and converts it into an object of generic type.
     *
     * @param json The JSON string to read.
     * @param <T>  The type of the object to return.
     * @return An object of the specified generic type parsed from the JSON string.
     * @throws Exception If there is an error while parsing the JSON.
     */
    public static <T> T readValueWithGenericType(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    /**
     * Reads JSON from the input stream and converts it into an object of generic type.
     *
     * @param inputStream The input stream containing the JSON data.
     * @param <T>         The type of the object to return.
     * @return An object of the specified generic type parsed from the JSON data in the input stream.
     * @throws IOException If there is an error while reading the input stream or parsing the JSON.
     */
    public static <T> T readValueWithGenericType(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, new TypeReference<>() {
        });
    }

    /**
     * Reads JSON string and converts it into an object of Specific type.
     *
     * @param content       The JSON string to read.
     * @param typeReference The typeReference of the object to return.
     * @param <T>           The type of the object to return.
     * @return An object of the specified generic type parsed from the JSON data in the input stream.
     * @throws IOException If there is an error while reading the input stream or parsing the JSON.
     */
    public static <T> T readValueWithType(String content, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(content, typeReference);
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param <T>    The type of the object to serialize.
     * @param object The object to serialize.
     * @return The JSON string representing the object.
     * @throws JsonProcessingException If there is an error during serialization.
     */
    public static <T> String objectToJsonString(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Updates the JSON structure for Object nodes by adding or modifying a key-value pair.
     *
     * @param json  The JSON string to update.
     * @param key   The key to add or modify.
     * @param value The value to set for the key.
     * @return The updated JSON string.
     * @throws Exception If there is an error while updating the JSON.
     */
    public static String updateJsonObject(String json, String key, String value) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);

        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            objectNode.put(key, value);
            return objectToJsonString(objectNode);
        } else {
            throw new IllegalArgumentException("Input JSON is not an object.");
        }
    }

    /**
     * Updates the JSON structure for Array nodes by adding a new JSON element.
     *
     * @param json    The JSON string to update.
     * @param element The JSON element to add to the array.
     * @return The updated JSON string.
     * @throws Exception If there is an error while updating the JSON.
     */
    public static String updateJsonArray(String json, String element) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(json);

        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            arrayNode.add(element);
            return objectToJsonString(arrayNode);
        } else {
            throw new IllegalArgumentException("Input JSON is not an array.");
        }
    }

    /**
     * Creates an ObjectNode with the specified key-value pair.
     *
     * @param key   The key for the JSON object.
     * @param value The value for the JSON object.
     * @return The created ObjectNode.
     */
    public static ObjectNode createObjectNode(String key, String value) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put(key, value);
        return jsonNode;
    }

    /**
     * Creates a list of ObjectNode instances with the specified key and a list of values, removing duplicates.
     *
     * @param key       The key for the JSON object.
     * @param valueList The list of values for the JSON object.
     * @return The list of created ObjectNode instances.
     */
    public static List<ObjectNode> createListObjectNode(String key, List<String> valueList) {
        List<ObjectNode> objectNodeList = new ArrayList<>();
        List<String> uniqueValues = new ArrayList<>(new HashSet<>(valueList)); // Remove duplicates
        for (String value : uniqueValues) {
            objectNodeList.add(createObjectNode(key, value));
        }
        return objectNodeList;
    }

    /**
     * Converts an object to a JSONArray.
     *
     * @param object The object to convert to a JSONArray.
     * @return A JSONArray representation of the object.
     * @throws Exception If an error occurs during the conversion process.
     */
    public static JSONArray objectToJsonArray(Object object) throws Exception {
        return new JSONArray(objectToJsonString(object));
    }

    /**
     * Converts an object to a Map&lt;String, Object&gt;.
     *
     * @param obj The object to convert.
     * @return A Map&lt;String, Object&gt; representing the object's fields.
     */
    public static Map<String, Object> convertObjectToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }
}
