package com.sismics.reader.core.model.jpa;

import javax.persistence.Entity;

import com.sismics.reader.core.constant.BugStatus;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_BUG_REPORT")
public class BugReport {
    @Id
    @Column(name = "BUG_ID_C", length = 36)
    private String id;

    @Column(name = "BUG_EMAIL_C", nullable = false, length = 100)
    private String email;

    @Column(name = "BUG_DESCRIPTION_C", nullable = false, length = 4000)
    private String description;

    @Column(name = "BUG_TIMESTAMP_D", nullable = false)
    private Long timestamp;

    @Column(name = "BUG_STATUS_C", nullable = false)
    private BugStatus status;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public BugStatus getStatus() {
        return status;
    }

    public void setStatus(BugStatus status) {
        this.status = status;
    }
}
