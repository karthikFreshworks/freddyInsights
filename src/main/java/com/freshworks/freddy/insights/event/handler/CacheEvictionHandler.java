package com.freshworks.freddy.insights.event.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshworks.freddy.insights.event.Event;
import com.freshworks.freddy.insights.event.cache.CacheEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CacheEvictionHandler implements EventHandler {
    public static final TypeReference<List<CacheEvent>> CACHE_LIST_DETAILS = new TypeReference<>() {};

    private final CacheManager cacheManager;

    private final ObjectMapper objectMapper;

    public CacheEvictionHandler(@Qualifier("inMemory") CacheManager cacheManager,
                                ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleEvent(Event event) {
        List<CacheEvent> cacheDetails = objectMapper.convertValue(event.getPayload(), CACHE_LIST_DETAILS);
        for (CacheEvent cacheEvent : cacheDetails) {
            try {
                log.info("Clearing Cache for {} {}", cacheEvent.getCacheName(), cacheEvent.getKey());
                invalidateCache(cacheEvent.getCacheName(), cacheEvent.getKey());
            } catch (Exception e) {
                log.error("An exception when clearing cache name {} cache key {}",
                        cacheEvent.getCacheName(), cacheEvent.getKey(), e);
            }
        }
    }

    @Override
    public Event.EventType getEventHandlerType() {
        return Event.EventType.INVALIDATE_CACHE;
    }

    public void invalidateCache(String cacheName, String keyName) {
        Cache phaseCache = cacheManager.getCache(cacheName);
        if (phaseCache != null) {
            if (keyName == null) {
                phaseCache.clear();
                log.info("Cache eviction full for cache: {}", phaseCache.getName());
            } else {
                boolean evicted = phaseCache.evictIfPresent(keyName);
                log.info("Cache eviction status: {} for key: {} in cache: {}", evicted, keyName, phaseCache.getName());
            }
            return;
        }
        log.warn("Non existent Cache {}", cacheName);
    }
}
