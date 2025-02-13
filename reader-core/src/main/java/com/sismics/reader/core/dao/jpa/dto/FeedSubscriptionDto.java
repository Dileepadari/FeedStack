```java
package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Job DTO.
 *
 * @author jtremeaux
 */
public class JobDto {

    /**
     * Job ID.
     */
    private String id;

    /**
     * Job name.
     */
    private String name;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Creation date.
     */
    private Date createDate;

    /**
     * Start date.
     */
    private Date startDate;

    /**
     * End date.
     */
    private Date endDate;

    /**
     * Job status.
     */
    private JobStatus status;

    public static enum JobStatus {
        CREATED,
        STARTED,
        FINISHED,
        FAILED
    }

    // TODO: Add getters and setters

    /**
     * Is the job finished?
     *
     * @return True if the job is finished, false otherwise
     */
    public boolean isFinished() {
        return status == JobStatus.FINISHED;
    }

    /**
     * Is the job failed?
     *
     * @return True if the job is failed, false otherwise
     */
    public boolean isFailed() {
        return status == JobStatus.FAILED;
    }
}
```