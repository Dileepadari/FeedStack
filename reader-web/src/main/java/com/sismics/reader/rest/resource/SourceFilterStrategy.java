package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import java.util.List;

/**
 * Strategy for filtering articles by sources (feed subscriptions).
 */
public class SourceFilterStrategy implements ArticleFilterStrategy {
    private List<String> sourceIds;

    public SourceFilterStrategy(List<String> sourceIds) {
        this.sourceIds = sourceIds;
    }

    @Override
    public UserArticleCriteria applyCriteria(UserArticleCriteriaBuilder builder) {
        if (!sourceIds.isEmpty()) {
            // If there's only one source, use the simple approach
            if (sourceIds.size() == 1) {
                builder.withFeedSubscriptionId(sourceIds.get(0));
            } else {
                // For multiple sources, use the new method
                builder.withFeedSubscriptionIds(sourceIds);
            }
        }
        return builder.build();
    }
}