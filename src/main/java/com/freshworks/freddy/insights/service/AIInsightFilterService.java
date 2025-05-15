package com.freshworks.freddy.insights.service;

import com.freshworks.freddy.insights.config.RedisClient;
import com.freshworks.freddy.insights.dto.insight.AIInsightFiltersDTO;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AIInsightFilterService extends AbstractAIBaseHelper  {
    private final RedisClient redisClient;

    public AIInsightFiltersDTO setAiInsightFilters(@Valid AIInsightFiltersDTO aiInsightFiltersDTO) {
        var insightFilterRedisKey = generateInsightFilterRedisKey("v0");
        return insightFilterRedisKey != null
                ? setInsightFilterRedisValue(insightFilterRedisKey, aiInsightFiltersDTO)
                : AIInsightFiltersDTO.builder().filters(new ArrayList<>()).build();
    }

    public AIInsightFiltersDTO getAiInsightFilters() {
        var insightFilterRedisKey = generateInsightFilterRedisKey("v0");
        return insightFilterRedisKey != null
                ? getInsightFilterRedisValue(insightFilterRedisKey)
                : AIInsightFiltersDTO.builder().filters(new ArrayList<>()).build();
    }

    public String generateInsightFilterRedisKey(String version) {
        var entityType = "insights";
        return Stream.of(getContextVO().getAiBundleEntity().getId(), getContextVO().getAccountId(),
                        getContextVO().getUserId(), version)
                .allMatch(Objects::nonNull)
                ? String.join("::", getContextVO().getAiBundleEntity().getId(), getContextVO().getAccountId(),
                getContextVO().getUserId(), entityType, version)
                : null;
    }

    @Cacheable(value = "insightFilters", key = "#key")
    public AIInsightFiltersDTO getInsightFilterRedisValue(String key) {
        log.info("Key which is generated : " + key);
        var insightFilterValue = redisClient.getValue(key);
        if (Objects.nonNull(insightFilterValue)) {
            try {
                log.info("Reading value as a string : " + key);
                return objectMapper.readValue(insightFilterValue, AIInsightFiltersDTO.class);
            } catch (Exception e) {
                log.info("Exception occurred while getting parsing the redis value with key: " + key
                        + " and with exception: " + e.getMessage());
                return AIInsightFiltersDTO.builder().filters(new ArrayList<>()).build();
            }
        }
        log.info("Value not found in redis for key : " + key);
        return AIInsightFiltersDTO.builder().filters(new ArrayList<>()).build();
    }

    @CacheEvict(value = "insightFilters", key = "#key")
    public AIInsightFiltersDTO setInsightFilterRedisValue(String key, AIInsightFiltersDTO value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisClient.setValue(key, json);
        } catch (Exception e) {
            log.info("Exception occurred while writing value as string with key: " + key
                    + " and with exception: " + e.getMessage());
            return AIInsightFiltersDTO.builder().filters(new ArrayList<>()).build();
        }
        return value;
    }
}
