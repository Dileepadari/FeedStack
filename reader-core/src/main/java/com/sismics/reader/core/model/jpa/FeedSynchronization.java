```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Synchronization of a feed.
 *
 * @author bgamard
 */
@Entity
@Table(name = "T_FEED_SYNCHRONIZATION")
public class FeedSynchronization {
    /**
     * Feed synchronization ID.
     */
    @Id
    @Column(name = "FSY_ID_C", length = 36)
    private String id;

    /**
     * Feed ID.
     */
    @Column(name = "FSY_IDFEED_C", nullable = false, length = 36)
    private String feedId;

    /**
     * Success status.
     */
    @Column(name = "FSY_SUCCESS_B", nullable = false)
    private boolean success;

    /**
     * Message.
     */
    @Lob
    @Column(name = "FSY_MESSAGE_C")
    private String message;

    /**
     * Duration (in milliseconds).
     */
    @Column(name = "FSY_DURATION_N", nullable = false)
    private Integer duration;

    /**
     * Creation date.
     */
    @Column(name = "FSY_CREATEDATE_D", nullable = false)
    private Date createDate;

    // getters and setters

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("feedId", feedId)
                .add("success", success)
                .toString();
    }
}
```