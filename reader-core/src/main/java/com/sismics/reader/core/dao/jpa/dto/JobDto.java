```java
package com.sismics.reader.core.dao.jpa.dto;

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
     * Feed category DTO.
     */
    private FeedCategoryDto feedCategoryDto;

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

    public FeedCategoryDto getFeedCategoryDto() {
        return feedCategoryDto;
    }

    public void setFeedCategoryDto(FeedCategoryDto feedCategoryDto) {
        this.feedCategoryDto = feedCategoryDto;
    }
}

/**
 * Feed category DTO.
 */
public class FeedCategoryDto {

    /**
     * Feed category ID.
     */
    private String id;

    /**
     * Feed category parent Id.
     */
    private String parentId;

    /**
     * Feed category name.
     */
    private String name;

    /**
     * True if this category is folded in the subscriptions tree.
     */
    private boolean folded;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }
}
```