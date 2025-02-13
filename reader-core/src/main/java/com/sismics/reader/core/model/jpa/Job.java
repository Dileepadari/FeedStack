```java
package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * Job.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_JOB")
public class Job {
    /**
     * Job ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "JOB_IDUSER_C", length = 36)
    private String userId;
    
    /**
     * Job name.
     */
    @Column(name = "JOB_NAME_C", length = 50, nullable = false)
    private String name;
    
    /**
     * Creation date.
     */
    @Column(name = "JOB_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Start date.
     */
    @Column(name = "JOB_STARTDATE_D")
    private Date startDate;
    
    /**
     * End date.
     */
    @Column(name = "JOB_ENDDATE_D")
    private Date endDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "JOB_DELETEDATE_D")
    private Date deleteDate;
    
    // ==================== Getters / Setters ==================== //
    
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    // ==================== Methods ==================== //
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}

```