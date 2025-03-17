package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import java.util.List;

/**
 * Strategy for filtering articles by categories.
 */
public class CategoryFilterStrategy implements ArticleFilterStrategy {
    private List<String> categoryIds;

    public CategoryFilterStrategy(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    @Override
    public UserArticleCriteria applyCriteria(UserArticleCriteriaBuilder builder) {
        if (!categoryIds.isEmpty()) {
            // If there's only one category, use the simple approach
            if (categoryIds.size() == 1) {
                builder.withCategoryId(categoryIds.get(0));
            } else {
                // For multiple categories, use the new method
                builder.withCategoryIds(categoryIds);
            }
        }
        return builder.build();
    }
}