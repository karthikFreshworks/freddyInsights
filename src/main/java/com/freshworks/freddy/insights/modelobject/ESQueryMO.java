package com.freshworks.freddy.insights.modelobject;

import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESQueryMO {
    private ArrayList<ESRuleMO> rules;
    private int page = 1;
    private int limit = 10;
    private ArrayList<String> sort;
    private boolean isSearch;
    private boolean isAutocomplete;

    private String collapse;

    public void setLimit(Integer limit) {
        this.limit = limit > 100 ? 100 : limit;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ESFaqQueryParams{");
        sb.append("rules=").append(rules);
        sb.append(", page=").append(page);
        sb.append(", per_page=").append(limit);
        sb.append(", sort=").append(sort);
        sb.append(", isSearch=").append(isSearch);
        sb.append(", isAutocomplete=").append(isAutocomplete);
        sb.append("}");
        return sb.toString();
    }
}
