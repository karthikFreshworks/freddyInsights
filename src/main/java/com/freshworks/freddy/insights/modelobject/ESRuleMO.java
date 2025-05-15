package com.freshworks.freddy.insights.modelobject;

import lombok.*;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.index.query.BoolQueryBuilder;

import java.util.List;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ESRuleMO {
    private String operand;
    private String operator;
    private String nestedOperand;
    private ScoreMode scoreMode = ScoreMode.None;
    private List<String> value;

    private BoolQueryBuilder queryBuilder;

    public String[] getValueArray() {
        String[] valueArray = new String[value.size()];
        for (int i = 0; i < valueArray.length; i++) {
            valueArray[i] = value.get(i);
        }
        return valueArray;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule{");
        sb.append("operand='").append(operand).append("'");
        sb.append(", operator='").append(operator).append("'");
        sb.append(", value='").append(value).append("'");
        sb.append(", nestedOperand").append(nestedOperand).append("'");
        sb.append("}");
        return sb.toString();
    }
}
