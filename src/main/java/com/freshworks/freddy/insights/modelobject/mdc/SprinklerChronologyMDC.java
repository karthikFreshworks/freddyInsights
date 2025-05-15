package com.freshworks.freddy.insights.modelobject.mdc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.helper.ObjectMapperHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;

/**
 * Utility class for handling Sprinkler Chronology data stored in Mapped Diagnostic Context (MDC).
 */
@Slf4j
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SprinklerChronologyMDC extends AbstractChronologyMDC {
    private List<Map<String, Object>> llmResponses;

    /**
     * Constructs a SprinklerChronologyMDC object by retrieving data from MDC.
     */
    public SprinklerChronologyMDC() {
        try {
            String json = MDC.get(ObservabilityConstant.SPRINKLER_RESPONSE_CHRONOLOGY);
            if (json != null && !json.isEmpty()) {
                llmResponses = ObjectMapperHelper.readLinkedListOfMaps(json);
            }
        } catch (Exception e) {
            log.error("Error processing JSON of Sprinkler response chronology: {}, CAUSE: {}", e.getMessage(),
                    ExceptionHelper.stackTrace(e));
        }
    }

    /**
     * Converts the SprinklerChronologyMDC object to its JSON representation.
     *
     * @return JSON representation of the object.
     */
    @Override
    public String toJson() {
        return ObjectMapperHelper.toJson(this, false, "Error parsing SprinklerChronologyMDC");
    }
}
