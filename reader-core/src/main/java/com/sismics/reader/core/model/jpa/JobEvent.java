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
 * Job event.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_JOB_EVENT")
public class JobEvent {
    /**
     * Job event ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOE_ID_C", length = 36)
    private String id;
    
    /**
     * Job ID.
     */
    @Column(name = "JOE_IDJOB_C", nullable = false, length = 36)
    private String jobId;
    
    /**
     * Job event name.
     */
    @Column(name = "JOE_NAME_C", length = 50)
    private String name;
    
    /**
     * Job event value.
     */
    @Column(name = "JOE_VALUE_C", length = 250)
    private String value;
    
    /**
     * Creation date.
     */
    @Column(name = "JOE_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "JOE_DELETEDATE_D")
    private Date deleteDate;
    
    // ==================== Getters / Setters ==================== //
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
                .add("value", value)
                .toString();
    }
}

```