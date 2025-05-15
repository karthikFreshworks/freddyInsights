package com.freshworks.freddy.insights.dto.bundle;

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
public class AIBundleUpdateDTO  extends AIBundleBaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
}
