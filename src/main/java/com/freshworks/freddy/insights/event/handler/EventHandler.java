package com.freshworks.freddy.insights.event.handler;

import com.freshworks.freddy.insights.event.Event;

public interface EventHandler {
    void handleEvent(Event event);

    Event.EventType getEventHandlerType();
}
