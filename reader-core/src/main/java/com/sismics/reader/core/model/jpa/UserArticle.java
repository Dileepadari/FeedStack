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
     * Subscription details.
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
     * Creation date.
     */
    @Column(name = "USA_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    public UserArticle() {
    }

    public UserArticle(String id, String userId, String articleId, Date createDate) {
        this.id = id;
        this.userId = userId;
        this.articleId = articleId;
        this.createDate = createDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("articleId", articleId)
                .add("createDate", createDate)
                .toString();
    }
}
```
====FILE_DELIMITER====
```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Subscription details for {@link UserArticle}.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER_ARTICLE_DETAILS")
public class UserArticleDetails {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "UAD_ID_C")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
```