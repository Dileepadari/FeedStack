package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import java.util.List;

/**
 * Builder for UserArticleCriteria.
 */
public class UserArticleCriteriaBuilder {
    private UserArticleCriteria criteria;

    public UserArticleCriteriaBuilder() {
        this.criteria = new UserArticleCriteria();
    }

    public UserArticleCriteriaBuilder withUserId(String userId) {
        criteria.setUserId(userId);
        return this;
    }

    public UserArticleCriteriaBuilder withUnread(boolean unread) {
        criteria.setUnread(unread);
        return this;
    }

    public UserArticleCriteriaBuilder withSubscribed(boolean subscribed) {
        criteria.setSubscribed(subscribed);
        return this;
    }

    public UserArticleCriteriaBuilder withVisible(boolean visible) {
        criteria.setVisible(visible);
        return this;
    }

    public UserArticleCriteriaBuilder withCategoryId(String categoryId) {
        criteria.setCategoryId(categoryId);
        return this;
    }

    public UserArticleCriteriaBuilder withCategoryIds(List<String> categoryIds) {
        criteria.setCategoryIdIn(categoryIds);
        return this;
    }

    public UserArticleCriteriaBuilder withFeedSubscriptionId(String feedSubscriptionId) {
        criteria.setFeedSubscriptionId(feedSubscriptionId);
        return this;
    }

    public UserArticleCriteriaBuilder withFeedSubscriptionIds(List<String> feedSubscriptionIds) {
        criteria.setFeedSubscriptionIdIn(feedSubscriptionIds);
        return this;
    }

    public UserArticleCriteria build() {
        return criteria;
    }

    public UserArticleCriteriaBuilder withArticleId(String articleId) {
        criteria.setArticleId(articleId);
        return this;
    }

    public UserArticleCriteriaBuilder withStarred(boolean starred) {
        criteria.setStarred(starred);
        return this;
    }
}