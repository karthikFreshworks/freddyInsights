package com.freshworks.freddy.insights.modelobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ESIndexMO<T> extends ESBaseMO {
    private T source;
    private boolean waitUntil;
}
