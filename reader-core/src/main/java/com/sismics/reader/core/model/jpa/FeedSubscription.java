```java
package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Subscription from a user to a feed.
 *
 * @author jtremeaux
 */
@Entity
@Table(name = "T_FEED_SUBSCRIPTION")
public class FeedSubscription {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "FES_ID_C", length = 36)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "FES_IDUSER_C", nullable = false, length = 36)
    private String userId;

    /**
     * Feed ID.
     */
    @Column(name = "FES_IDFEED_C", nullable = false, length = 36)
    private String feedId;

    /**
     * Category ID.
     */
    @Column(name = "FES_IDCATEGORY_C", nullable = false, length = 36)
    private String categoryId;

    /**
     * Subscription title (overrides feed title).
     */
    @Column(name = "FES_TITLE_C", length = 100)
    private String title;

    // getters and setters

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("feedId", feedId)
                .add("title", title)
                .toString();
    }
}
```