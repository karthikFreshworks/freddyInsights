package com.freshworks.freddy.insights.modelobject;

import com.freshworks.freddy.insights.constant.enums.ESIndexNameEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ESBaseMO {
    private String indexKey;
    private String routingKey;
    private ESIndexNameEnum indexName;
}
