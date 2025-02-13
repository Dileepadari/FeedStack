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
    private String feedSubscriptionTitle;

    /**
     * Feed Details.
     */
    private FeedDto feed;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Number of unread articles by this user in this subscription.
     */
    private Integer unreadUserArticleCount;

    /**
     * Number of synchronization fails recently.
     */
    private Integer synchronizationFailCount;

    /**
     * Create date.
     */
    private Date createDate;

    /**
     * Category.
     */
    private CategoryDto category;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of feedSubscriptionTitle.
     *
     * @return feedSubscriptionTitle
     */
    public String getFeedSubscriptionTitle() {
        return feedSubscriptionTitle;
    }

    /**
     * Setter of feedSubscriptionTitle.
     *
     * @param feedSubscriptionTitle feedSubscriptionTitle
     */
    public void setFeedSubscriptionTitle(String feedSubscriptionTitle) {
        this.feedSubscriptionTitle = feedSubscriptionTitle;
    }

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of feed.
     *
     * @return feed
     */
    public FeedDto getFeed() {
        return feed;
    }

    /**
     * Setter of feed.
     *
     * @param feed feed
     */
    public void setFeed(FeedDto feed) {
        this.feed = feed;
    }

    /**
     * Getter of unreadUserArticleCount.
     *
     * @return unreadUserArticleCount
     */
    public Integer getUnreadUserArticleCount() {
        return unreadUserArticleCount;
    }

    /**
     * Setter of unreadUserArticleCount.
     *
     * @param unreadUserArticleCount unreadUserArticleCount
     */
    public void setUnreadUserArticleCount(Integer unreadUserArticleCount) {
        this.unreadUserArticleCount = unreadUserArticleCount;
    }

    /**
     * Getter of category.
     *
     * @return category
     */
    public CategoryDto getCategory() {
        return category;
    }

    /**
     * Setter of category.
     *
     * @param category category
     */
    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of synchronizationFailCount.
     *
     * @return synchronizationFailCount
     */
    public Integer getSynchronizationFailCount() {
        return synchronizationFailCount;
    }

    /**
     * Setter of synchronizationFailCount.
     *
     * @param synchronizationFailCount synchronizationFailCount
     */
    public void setSynchronizationFailCount(Integer synchronizationFailCount) {
        this.synchronizationFailCount = synchronizationFailCount;
    }
}
