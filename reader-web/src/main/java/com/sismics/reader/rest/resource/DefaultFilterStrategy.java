package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;

/**
 * Default strategy for filtering articles (no filters).
 */
public class DefaultFilterStrategy implements ArticleFilterStrategy {
    @Override
    public UserArticleCriteria applyCriteria(UserArticleCriteriaBuilder builder) {
        // No additional criteria
        return builder.build();
    }
}