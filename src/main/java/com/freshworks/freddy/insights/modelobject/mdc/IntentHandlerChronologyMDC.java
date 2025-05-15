package com.freshworks.freddy.insights.modelobject.mdc;

import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class IntentHandlerChronologyMDC extends AbstractChronologyMDC {
    private String duration;
    private String statusCode;
    private String responseCode;

    @Override
    public String toJson() {
        return ObjectMapperHelper.toJson(this, false, "Error parsing IntentHandlerChronologyMDC");
    }
}
