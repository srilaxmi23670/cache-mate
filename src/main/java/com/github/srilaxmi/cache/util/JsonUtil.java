package com.github.srilaxmi.cache.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@UtilityClass
@Slf4j
public class JsonUtil {


    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convenience method for converting any object to Json
     */
    public JsonNode toJson(Object objectToConvert) {
        return objectMapper.convertValue(objectToConvert, JsonNode.class);
    }

    public <T> T fromJson(JsonNode jsonNode, Class<T> clazz) {
        return objectMapper.convertValue(jsonNode, clazz);
    }

    public <T> T fromJsonString(String jsonString, Class<T> clazz, TypeReference<T> typeReference) {
        if (clazz != null) {
            return fromJsonString(jsonString, clazz);
        } else {
            return fromJsonString(jsonString, typeReference);
        }
    }

    public <T> T fromJsonString(String jsonString, Class<T> clazz) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.convertValue(objectMapper.readTree(jsonString), clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize object {} to class {}", jsonString, clazz.getName());
            return null;
        }
    }

    public <T> T fromJsonString(String jsonString, TypeReference<T> clazz) {
        try {
            return objectMapper.convertValue(objectMapper.readTree(jsonString), clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize object {}", jsonString);
            return null;
        }
    }

    public String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public <T> T convert(Object object, Class<T> clazz) {
        return fromJson(toJson(object), clazz);
    }

    /**
     * Removes all those key-value pairs whose values are null.
     */
    public void cleanNulls(ObjectNode objectNode) {
        List<String> nullKeys = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
        iterator.forEachRemaining(entry -> {
            if (entry.getValue() == null || entry.getValue().isNull())
                nullKeys.add(entry.getKey());
        });

        for (String key: nullKeys)
            objectNode.remove(key);
    }

    /** Parse a String representing a json, and return it as a JsonNode. */
    public JsonNode parse(String src) {
        try {
            return objectMapper.readTree(src);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}


