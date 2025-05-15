package com.freshworks.freddy.insights.event.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class CacheEvent {
    private String cacheName;

    private String key;
}
