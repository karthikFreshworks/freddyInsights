package com.freshworks.freddy.insights.modelobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ESResponseMO<T> {
    private long count;
    private boolean nextPage;
    private List<T> records = new ArrayList<>();
}
