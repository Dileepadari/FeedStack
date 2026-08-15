package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import java.util.List;

/**
 * Strategy for filtering articles by both categories and sources.
 */
public class CombinedFilterStrategy implements ArticleFilterStrategy {
    private List<String> categoryIds;
    private List<String> sourceIds;

    public CombinedFilterStrategy(List<String> categoryIds, List<String> sourceIds) {
        this.categoryIds = categoryIds;
        this.sourceIds = sourceIds;
    }

    @Override
    public UserArticleCriteria applyCriteria(UserArticleCriteriaBuilder builder) {
        // Handle multiple categories
        if (!categoryIds.isEmpty()) {
            if (categoryIds.size() == 1) {
                builder.withCategoryId(categoryIds.get(0));
            } else {
                builder.withCategoryIds(categoryIds);
            }
        }
        
        // Handle multiple sources
        if (!sourceIds.isEmpty()) {
            if (sourceIds.size() == 1) {
                builder.withFeedSubscriptionId(sourceIds.get(0));
            } else {
                builder.withFeedSubscriptionIds(sourceIds);
            }
        }
        
        return builder.build();
    }
}