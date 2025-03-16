package com.sismics.reader.core.dao.jpa.criteria;

import com.sismics.reader.core.constant.BugStatus;
import java.util.Date;

public class BugReportCriteria {
    private String id;
    private String email;
    private String description;
    private Date timestamp;
    private BugStatus status;

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public BugStatus getStatus() {
        return status;
    }

    public BugReportCriteria setId(String id) {
        this.id = id;
        return this;
    }

    public BugReportCriteria setEmail(String email) {
        this.email = email;
        return this;
    }

    public BugReportCriteria setDescription(String description) {
        this.description = description;
        return this;
    }

    public BugReportCriteria setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public BugReportCriteria setStatus(BugStatus status) {
        this.status = status;
        return this;
    }
}
