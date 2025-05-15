package com.freshworks.freddy.insights.dto.insight;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.freshworks.freddy.insights.constant.AIInsightConstant;
import com.freshworks.freddy.insights.constant.ExceptionConstant;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "userInsightMapping")
@Builder
public class AIInsightsDismissDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    @NotNull(message = ExceptionConstant.NOT_VALID_INSIGHT_ID)
    private String insightId;
    @Builder.Default
    private boolean dismissed = true;
    @NotNull(message = ExceptionConstant.NOT_VALID_TENANT)
    private TenantEnum tenant = TenantEnum.global;
    private String accountId = AIInsightConstant.DEFAULT_ACCOUNT;
    @NotNull(message = ExceptionConstant.NOT_VALID_USER_ID)
    private String userId = AIInsightConstant.DEFAULT_USER_ID;
    private String orgId;
    private String bundleId;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date timeToLive;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private String createdAt;
    @NotNull(message = ExceptionConstant.NOT_VALID_USER_ID)
    private String createdBy;
}
