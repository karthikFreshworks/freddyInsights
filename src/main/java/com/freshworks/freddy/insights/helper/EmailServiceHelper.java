package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.dto.email.EmailRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class EmailServiceHelper {
    private AppConfigHelper appConfigHelper;
    private HttpConnectionHelper httpConnectionHelper;

    @Autowired
    public void setHttpConnectionHelper(@Lazy HttpConnectionHelper httpConnectionHelper) {
        this.httpConnectionHelper = httpConnectionHelper;
    }

    @Autowired
    public void setAppConfigHelper(AppConfigHelper appConfigHelper) {
        this.appConfigHelper = appConfigHelper;
    }

    public void send(EmailRequestDTO emailRequestDTO) {
        try {
            HttpPost httpPost = new HttpPost(appConfigHelper.getEmailbotServiceHost());
            StringEntity stringEntity = new StringEntity(emailRequestDTO.toJson(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Authorization", appConfigHelper.getEmailbotServiceAuth());
            httpPost.setHeader("Content-Type", "application/json");
            httpConnectionHelper.builder(httpPost)
                    .withThrowWhenRetryOnResponseExceeded(true)
                    .withMaxAttempts(appConfigHelper.getHttpConnectionMaxRetry())
                    .withRetryDelay(Duration.ofMillis(appConfigHelper.getHttpConnectionDelayMillis()))
                    .buildApacheHttpAsync()
                    .connect()
                    .thenAccept(response -> {
                        int statusCode = response.getStatusCode();
                        String responseBody = response.getBody();
                        log.info("Response from Email service: status code {}, response {}", statusCode, responseBody);
                    })
                    .exceptionally(ex -> {
                        log.info("Exception occurred from Email service {} ", ExceptionHelper.stackTrace(ex));
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error occurred on while sending email {}", ExceptionHelper.stackTrace(e));
        }
    }
}
