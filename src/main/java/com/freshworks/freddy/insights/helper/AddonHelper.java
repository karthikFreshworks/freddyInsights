package com.freshworks.freddy.insights.helper;

import com.freshworks.freddy.insights.constant.enums.AddonEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AddonHelper extends AbstractAIBaseHelper{

    public boolean isSelfServiceAddonAvailable() {
        if (getContextVO().getAddons() != null) {
            return containsExactMatch(getContextVO().getAddons(), AddonEnum.selfservice.getValue());
        } else {
            return false;
        }
    }

    public boolean isOnlySelfServiceAddonAvailable() {
        if (getContextVO().getAddons() != null) {
            return isSelfServiceAddonAvailable() && !isInsightAddonAvailable() && !isCopilotAddonAvailable();
        } else {
            return false;
        }
    }

    public boolean isInsightAddonAvailable() {
        if (getContextVO().getAddons() != null) {
            return containsExactMatch(getContextVO().getAddons(), AddonEnum.insights.getValue());
        } else {
            return false;
        }
    }

    public boolean isCopilotAddonAvailable() {
        if (getContextVO().getAddons() != null) {
            return containsExactMatch(getContextVO().getAddons(), AddonEnum.copilot.getValue());
        } else {
            return false;
        }
    }

    public boolean containsExactMatch(List<String> strings, String targetString) {
        return strings.stream().anyMatch(targetString::equals);
    }
}
