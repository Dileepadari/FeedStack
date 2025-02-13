```java
package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Subscription from a user to an article.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER_ARTICLE")
public class UserArticle {
    /**
     * UserArticle details.
     */
    private UserArticleDetails details;
    
    /**
     * Creation date.
     */
    @Column(name = "USA_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Default constructor.
     */
    public UserArticle() {
        this.details = new UserArticleDetails();
    }
    
    /**
     * Constructor.
     * 
     * @param details UserArticle details
     * @param createDate Creation date
     */
    public UserArticle(UserArticleDetails details, Date createDate) {
        this.details = details;
        this.createDate = createDate;
    }
    
    /**
     * Getter of details.
     *
     * @return details
     */
    public UserArticleDetails getDetails() {
        return details;
    }

    /**
     * Setter of details.
     *
     * @param details details
     */
    public void setDetails(UserArticleDetails details) {
        this.details = details;
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("details", details)
                .add("createDate", createDate)
                .toString();
    }
    
    /**
     * UserArticle details.
     */
    public static class UserArticleDetails {
        /**
         * Subscription ID.
         */
        @Id
        @Column(name = "USA_ID_C", length = 36)
        private String id;
        
        /**
         * User ID.
         */
        @Column(name = "USA_IDUSER_C", nullable = false, length = 36)
        private String userId;
        
        /**
         * Article ID.
         */
        @Column(name = "USA_IDARTICLE_C", nullable = false, length = 36)
        private String articleId;
        
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
         * Getter of articleId.
         *
         * @return articleId
         */
        public String getArticleId() {
            return articleId;
        }

        /**
         * Setter of articleId.
         *
         * @param articleId articleId
         */
        public void setArticleId(String articleId) {
            this.articleId = articleId;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("id", id)
                    .add("userId", userId)
                    .add("articleId", articleId)
                    .toString();
        }
    }
}
```