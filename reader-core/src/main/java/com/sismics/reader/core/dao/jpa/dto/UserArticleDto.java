package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * User article DTO.
 *
 * @author jtremeaux
 */
public class UserArticleDto {
    /**
     * User article ID.
     */
    private String id;

    /**
     * Date the user read this article.
     */
    private Long readTimestamp;

    /**
     * Date the user star this article.
     */
    private Long starTimestamp;

    /**
     * Feed title.
     */
    private String feedTitle;

    /**
     * Feed subscription ID.
     */
    private String feedSubscriptionId;

    /**
     * Feed subscription title.
     */
    private String feedSubscriptionTitle;

    /**
     * Article.
     */
    private ArticleDto article;

    /**
     * Publication date.
     */
    private Long articlePublicationTimestamp;

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
     * Getter of readTimestamp.
     *
     * @return readTimestamp
     */
    public Long getReadTimestamp() {
        return readTimestamp;
    }

    /**
     * Setter of readTimestamp.
     *
     * @param readTimestamp readTimestamp
     */
    public void setReadTimestamp(Long readTimestamp) {
        this.readTimestamp = readTimestamp;
    }

    /**
     * Getter of starTimestamp.
     *
     * @return starTimestamp
     */
    public Long getStarTimestamp() {
        return starTimestamp;
    }

    /**
     * Setter of starTimestamp.
     *
     * @param starTimestamp starTimestamp
     */
    public void setStarTimestamp(Long starTimestamp) {
        this.starTimestamp = starTimestamp;
    }

    /**
     * Getter of feedTitle.
     *
     * @return feedTitle
     */
    public String getFeedTitle() {
        return feedTitle;
    }

    /**
     * Setter of feedTitle.
     *
     * @param feedTitle feedTitle
     */
    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    /**
     * Getter of feedSubscriptionId.
     *
     * @return feedSubscriptionId
     */
    public String getFeedSubscriptionId() {
        return feedSubscriptionId;
    }

    /**
     * Setter of feedSubscriptionId.
     *
     * @param feedSubscriptionId feedSubscriptionId
     */
    public void setFeedSubscriptionId(String feedSubscriptionId) {
        this.feedSubscriptionId = feedSubscriptionId;
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
     * Getter of article.
     *
     * @return article
     */
    public ArticleDto getArticle() {
        return article;
    }

    /**
     * Setter of article.
     *
     * @param article article
     */
    public void setArticle(ArticleDto article) {
        this.article = article;
    }

    /**
     * Getter of articlePublicationTimestamp.
     *
     * @return articlePublicationTimestamp
     */
    public Long getArticlePublicationTimestamp() {
        return articlePublicationTimestamp;
    }

    /**
     * Setter of articlePublicationTimestamp.
     *
     * @param articlePublicationTimestamp articlePublicationTimestamp
     */
    public void setArticlePublicationTimestamp(Long articlePublicationTimestamp) {
        this.articlePublicationTimestamp = articlePublicationTimestamp;
    }
}
