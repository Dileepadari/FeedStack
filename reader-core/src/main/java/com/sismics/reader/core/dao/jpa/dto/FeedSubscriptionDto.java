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
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
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
     * Getter of startDate.
     *
     * @return startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Setter of startDate.
     *
     * @param startDate startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter of endDate.
     *
     * @return endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Setter of endDate.
     *
     * @param endDate endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
```