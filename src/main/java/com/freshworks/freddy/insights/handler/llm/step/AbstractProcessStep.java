package com.freshworks.freddy.insights.handler.llm.step;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.exception.ErrorCode;
import com.freshworks.freddy.insights.helper.AbstractAIBaseHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import com.freshworks.freddy.insights.modelobject.AIServiceMO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Abstract base class for implementing steps in an AI service pipeline.
 */
@Slf4j
public abstract class AbstractProcessStep extends AbstractAIBaseHelper implements Step<AIServiceMO> {
    protected final String stepName;

    public AbstractProcessStep(String stepName) {
        this.stepName = stepName;
    }

    /**
     * Modifies a URL by removing query parameters containing placeholders in their values.
     *
     * @param url    The original URL to be modified. Must not be null.
     * @param prefix The prefix to be removed from the values of query parameters. Must not be null.
     * @param suffix The suffix to be removed from the values of query parameters. Must not be null.
     * @return The modified URL without query parameters containing the given placeholder in their values.
     *         If the original URL does not contain query parameters, returns the original URL.
     * @throws IllegalArgumentException If any of the parameters (url, prefix, suffix) is null.
     */
    protected String removeQueryParamsWithPlaceholder(
            @NotNull String url, @NotNull String prefix, @NotNull String suffix) {
        String[] urlParts = url.split("\\?");
        if (urlParts.length != 2) {
            return url;
        }

        String domainPath = urlParts[0];
        String queryParams = urlParts[1];

        String[] queryParamParts = queryParams.split("&");
        StringBuilder modifiedURL = new StringBuilder(domainPath);

        if (queryParamParts.length > 0) {
            modifiedURL.append("?");
        }

        boolean firstQueryParamAppended = false;

        for (String queryParam : queryParamParts) {
            String[] keyValue = queryParam.split("=");
            if (keyValue.length == 2
                    && !(keyValue[1].startsWith(prefix) && keyValue[1].endsWith(suffix))) {
                if (firstQueryParamAppended) {
                    modifiedURL.append("&");
                }
                modifiedURL.append(queryParam);
                if (!firstQueryParamAppended) {
                    firstQueryParamAppended = true;
                }
            }
        }
        return modifiedURL.toString();
    }

    /**
     * Executes the current AI service step with exception handling.
     *
     * @param input The input data for the AI service step.
     * @return The modified input after executing the step.
     * @throws AIResponseStatusException If an error occurs during execution.
     */
    @Override
    public AIServiceMO execute(AIServiceMO input) {
        try {
            if (!shouldSkipStep(input)) {
                log.info("Executing {} LLM step.", stepName);
                executeStep(input);
            }
            return executeNextStep(input);
        } catch (AIResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            String message = String.format("An unexpected error occurred while executing LLM Step '%s'."
                    + " Message: %s", stepName, e.getMessage());
            log.error(message + "Cause: {}", ExceptionHelper.stackTrace(e));
            throw new AIResponseStatusException(message,
                    HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles exceptions and throws AIResponseStatusException.
     *
     * @param e             The exception that occurred.
     * @param httpStatus    The HTTP status to be returned.
     * @param errorCode     The error code for the exception.
     * @param messageFormat The format string for the error message.
     * @param args          Arguments to be included in the error message.
     * @throws AIResponseStatusException Always thrown with the specified error details.
     */
    protected void handleException(Exception e, HttpStatus httpStatus, ErrorCode errorCode, String messageFormat,
                                   Object... args) throws AIResponseStatusException {
        String message = String.format(messageFormat, args);
        log.error(message + " CAUSE : {}", ExceptionHelper.stackTrace(e));
        throw new AIResponseStatusException(message, httpStatus, errorCode);
    }

    /**
     * Executes the main logic of the step.
     *
     * @param input The input for the step.
     * @throws AIResponseStatusException If an error occurs during execution.
     */
    protected abstract void executeStep(AIServiceMO input) throws AIResponseStatusException;

    /**
     * Executes the next step in the pipeline.
     *
     * @param input The input for the step.
     * @return The result produced by the next step.
     * @throws AIResponseStatusException If an error occurs during execution.
     */
    protected abstract AIServiceMO executeNextStep(AIServiceMO input) throws AIResponseStatusException;

    /**
     * Determines whether the step should be skipped based on the input.
     *
     * @param input The input for the step.
     * @return {@code true} if the step should be skipped, {@code false} otherwise.
     */
    protected abstract boolean shouldSkipStep(AIServiceMO input);
}
