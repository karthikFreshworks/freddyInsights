package com.freshworks.freddy.insights.modelobject.mdc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChronologyMDC {
    protected abstract String toJson();
}
