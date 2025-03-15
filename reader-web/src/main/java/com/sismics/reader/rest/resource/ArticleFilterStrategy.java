package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;

/**
 * Strategy interface for filtering articles.
 */
public interface ArticleFilterStrategy {
    /**
     * Apply the filtering strategy to the criteria builder.
     *
     * @param builder The criteria builder
     * @return The built UserArticleCriteria
     */
    UserArticleCriteria applyCriteria(UserArticleCriteriaBuilder builder);
}