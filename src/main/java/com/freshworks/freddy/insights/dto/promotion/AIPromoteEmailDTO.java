package com.freshworks.freddy.insights.dto.promotion;

import com.freshworks.freddy.insights.constant.enums.RegionEnum;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIPromoteEmailDTO {
    private Map<RegionEnum, RegionStatus> regionStatusMap;

    @Data
    @AllArgsConstructor
    public static class RegionStatus {
        private List<String> successServiceIds;
        private Map<String, String> failureServiceIds;

        public RegionStatus() {
            successServiceIds = new ArrayList<>();
            failureServiceIds = new HashMap<>();
        }

        public String getFailureServiceIdsWithMessages() {
            String result = "";
            int i = 1;
            for (Map.Entry<String, String> entry : failureServiceIds.entrySet()) {
                result += String.format("%d. %s: %s<br>", i, entry.getKey(), entry.getValue());
                i++;
            }
            return result;
        }
    }
}
