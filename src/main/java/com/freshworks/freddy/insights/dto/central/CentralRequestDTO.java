package com.freshworks.freddy.insights.dto.central;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.freshworks.freddy.insights.constant.AIRequestConstant;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CentralRequestDTO<T> {
    private final String payloadType; // run_create
    private final T payload;
    private final String accountId;
    private String payloadVersion;

    public CentralRequestDTO(String payloadType, T payload) {
        this.payloadType = payloadType;
        this.payload = payload;
        this.accountId = MDC.get(AIRequestConstant.X_FW_AUTH_ACCOUNT_ID) == null
                ? "NA" : MDC.get(AIRequestConstant.X_FW_AUTH_ACCOUNT_ID);
    }

    public void setPayloadVersion(String payloadVersion) {
        this.payloadVersion = payloadVersion;
    }
}
