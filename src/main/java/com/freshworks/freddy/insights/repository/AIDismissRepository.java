package com.freshworks.freddy.insights.repository;

import com.freshworks.freddy.insights.dto.insight.AIInsightsDismissDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIDismissRepository extends MongoRepository<AIInsightsDismissDTO, String> {
    AIInsightsDismissDTO findByInsightId(String insightId, String userId);

    AIInsightsDismissDTO deleteByInsightId(String insightId, String userId);

    List<AIInsightsDismissDTO> findByUserId(String userId);
}
