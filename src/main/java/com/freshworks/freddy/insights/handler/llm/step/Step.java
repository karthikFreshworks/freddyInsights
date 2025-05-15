package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;

/**
 * Interface representing a step in an AI service pipeline.
 *
 * @param <R> The type of result produced by the step.
 */
public interface Step<R> {
    /**
     * Executes the step with the given input.
     *
     * @param input The input for the step.
     * @return The result produced by the step.
     * @throws AIResponseStatusException If an error occurs during execution.
     */
    R execute(AIServiceMO input) throws AIResponseStatusException;
}
