package com.freshworks.freddy.insights.handler.observability;

import com.freshworks.freddy.insights.constant.ObservabilityConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component(ObservabilityConstant.COMMON_OBSERVABILITY)
public class AICommonObservabilityHandlerImpl extends AbstractObservabilityHandler {
    @Override
    public StringBuilder getLogBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append(ObservabilityConstant.HOST)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.HOST, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.URI)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.URI, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_RQ_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_REQUEST_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.TRC_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.TRACE_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_FWD_FR)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_FORWARDED_FOR, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.START_TIME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.START_TIME, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.END_TIME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.END_TIME, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.STATUS)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.STATUS, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.METHOD)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.METHOD, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.DURATION)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.DURATION, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.TNT)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.TENANT, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.TNT_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.TENANT_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_ACNT_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_AUTH_ACCOUNT_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_BNDL_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_BUNDLE_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_USR_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_AUTH_USER_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_ORG_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_AUTH_ORG_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_GRP_ID)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_AUTH_GROUP_ID, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_DMN)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_AUTH_DOMAIN, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_BNDL_NME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FREDDY_AI_PLATFORM_BUNDLE, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_FDY_ADONS)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_FREDDY_ADDONS, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.X_FW_DYNM_HDRS)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.X_FW_DYNAMIC_HEADERS, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.DOCDB_RES_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MONGODB_DURATION, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.REDIS_RES_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.REDIS_DURATION, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.OPNSRCH_RES_TME)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.OPENSEARCH_DURATION, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.DOCDB_CAL_CNT)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MONGODB_CALLS_COUNT, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.REDIS_CAL_CNT)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.REDIS_CALLS_COUNT, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.OPNSRCH_CAL_CNT)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.OPENSEARCH_CALLS_COUNT, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.DOCDB_RES_TME_FLO)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MONGODB_DURATION_FLOW, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.REDIS_RES_TME_FLO)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.REDIS_DURATION_FLOW, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.OPNSRCH_RES_TME_FLO)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.OPENSEARCH_DURATION_FLOW, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.DOCDB_ERR_STUS_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MONGODB_ERROR_STATUS_CODE, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.REDIS_ERR_STUS_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.REDIS_ERROR_STATUS_CODE, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.OPNSRCH_ERR_STUS_CDE)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.OPENSEARCH_ERROR_STATUS_CODE, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.DOCDB_ERR_MSG)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.MONGODB_ERROR_MESSAGE, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.REDIS_ERR_MSG)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.REDIS_ERROR_MESSAGE, EMPTY_STRING))
                .append(DELIMITER);
        sb.append(ObservabilityConstant.OPNSRCH_ERR_MSG)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.OPENSEARCH_ERROR_MESSAGE, EMPTY_STRING))
                .append(DELIMITER);

        sb.append(ObservabilityConstant.ERROR)
                .append(ASSIGNMENT)
                .append(getOrDefault(ObservabilityConstant.ERROR, EMPTY_STRING));
        return sb;
    }

    @Override
    public void recordMetrics() {
        String tenantName = getOrDefault(ObservabilityConstant.TENANT, NA_STRING);

        recordMetric(ObservabilityConstant.MONGODB_DURATION, tenantName);
        recordMetric(ObservabilityConstant.OPENSEARCH_DURATION, tenantName);
        recordMetric(ObservabilityConstant.REDIS_DURATION, tenantName);
    }

    private void recordMetric(String metricName, String tenantName) {
        String durationValue = MDC.get(metricName);
        if (durationValue != null) {
            MetricBuilder builder = new MetricBuilder(metricName)
                    .withTag(ObservabilityConstant.TENANT, tenantName);
            builder.buildTimer().record(Duration.ofMillis(Long.parseLong(durationValue)));
        }
    }
}
