package com.sismics.reader.core.dao.jpa.criteria;

import com.sismics.reader.core.constant.BugStatus;

public class BugReportCriteria {
    private String id;
    private String email;
    private String description;
    private Long timestamp;
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

    public Long getTimestamp() {
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

    public BugReportCriteria setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public BugReportCriteria setStatus(BugStatus status) {
        this.status = status;
        return this;
    }
}
