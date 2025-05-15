package com.freshworks.freddy.insights.valueobject;

import com.freshworks.freddy.insights.constant.enums.PlatformEnum;
import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import com.freshworks.freddy.insights.dto.AITokenLimitDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AITokenLimitVO {
    TenantEnum tenant;
    PlatformEnum platform;
    String modelId;
    String accountId;
    String userID;
    AITokenLimitDTO requestBody;
}
