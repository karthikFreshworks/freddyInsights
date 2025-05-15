package com.freshworks.freddy.insights.modelobject;

import com.freshworks.freddy.insights.constant.enums.TenantEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ESSearchMO extends ESBaseMO {
    @NotNull
    private String[] include = new String[0];
    @NotNull
    private String[] exclude = new String[0];
    @NotNull
    private ESQueryMO esQueryMO;
    @NotNull
    private List<@NotNull TenantEnum> tenants;
}
