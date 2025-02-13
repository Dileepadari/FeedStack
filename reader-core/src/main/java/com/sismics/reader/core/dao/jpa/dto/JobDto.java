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
     * Feed category ID.
     */
    private String feedCategoryId;

    /**
     * True if this subscription is folded in the subscriptions tree.
     */
    private boolean folded;

    // TODO: Add getters and setters

    /**
     * Is this subscription folded?
     *
     * @return True if this subscription is folded, false otherwise
     */
    public boolean isFolded() {
        return folded;
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

    // TODO: Add getters and setters
}
```