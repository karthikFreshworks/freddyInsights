package com.freshworks.freddy.insights;

import com.fasterxml.jackson.core.type.TypeReference;
import com.freshworks.freddy.insights.event.Event;
import com.freshworks.freddy.insights.event.handler.EventHandler;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.freshworks.freddy.insights.constant.AIRequestConstant.X_REQUEST_ID;

@Component
@Slf4j
public class RedisEventListener {
    private static final int RETRY_DELAY = 500;
    public static final TypeReference<Event> TYPE_REFERENCE = new TypeReference<>() {
    };
    private final Map<Event.EventType, EventHandler> eventTypehandlerMap;

    private final String podName;

    public RedisEventListener(ApplicationContext applicationContext,
                              @Value("${pod.name}") String pod) {
        this.podName = pod;
        Map<Event.EventType, EventHandler> tempMap = new HashMap<>();
        applicationContext.getBeansOfType(EventHandler.class).forEach((beanName, handler) ->
                tempMap.put(handler.getEventHandlerType(), handler));
        this.eventTypehandlerMap = Collections.unmodifiableMap(tempMap);
    }

    @Retryable(value = EventProcessingFailedException.class, backoff = @Backoff(delay = RETRY_DELAY))
    public void onMessage(String message) throws Exception {
        try {
            MDC.put(X_REQUEST_ID, UUID.randomUUID().toString());
            Event event = ObjectMapperHelper.readValueWithType(message, TYPE_REFERENCE);
            log.info("Got Event for {} Pod {}", event, podName);
            if (event != null) {
                processEvent(event);
            } else {
                log.warn("Got invalid event  ");
            }
        } finally {
            MDC.clear();
        }
    }

    private void processEvent(Event event) {
        Optional.ofNullable(eventTypehandlerMap.get(event.getEventType()))
                .ifPresent(eventHandler -> {
                    try {
                        eventHandler.handleEvent(event);
                    } catch (Exception e) {
                        throw new EventProcessingFailedException(e);
                    }
                });
    }

    private static class EventProcessingFailedException extends RuntimeException {
        public EventProcessingFailedException(Throwable throwable) {
            super(throwable);
        }
    }
}
