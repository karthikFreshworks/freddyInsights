package com.freshworks.freddy.insights.dto.tenant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@ToString
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AITenantUpdateDTO extends AITenantBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
}
