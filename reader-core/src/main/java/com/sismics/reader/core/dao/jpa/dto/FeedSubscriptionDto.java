```java
package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Feed subscription DTO.
 *
 * @author jtremeaux 
 */
public class FeedSubscriptionDto {
    /**
     * Feed subscription ID.
     */
    private String id;

    /**
     * Feed subscription title.
     */
    private String title;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Feed ID.
     */
    private String feedId;

    /**
     * Feed RSS URL.
     */
    private String feedRssUrl;

    /**
     * Feed URL.
     */
    private String feedUrl;

    /**
     * Feed description.
     */
    private String feedDescription;

    /**
     * Category ID.
     */
    private String categoryId;

    /**
     * Category parent Id.
     */
    private String categoryParentId;

    /**
     * Category name.
     */
    private String categoryName;

    /**
     * True if this category is folded in the subscriptions tree.
     */
    private boolean categoryFolded;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedRssUrl() {
        return feedRssUrl;
    }

    public void setFeedRssUrl(String feedRssUrl) {
        this.feedRssUrl = feedRssUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getFeedDescription() {
        return feedDescription;
    }

    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryParentId() {
        return categoryParentId;
    }

    public void setCategoryParentId(String categoryParentId) {
        this.categoryParentId = categoryParentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isCategoryFolded() {
        return categoryFolded;
    }

    public void setCategoryFolded(boolean categoryFolded) {
        this.categoryFolded = categoryFolded;
    }
}
```