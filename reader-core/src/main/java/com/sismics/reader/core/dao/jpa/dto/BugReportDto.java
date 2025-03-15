package com.sismics.reader.core.dao.jpa.dto;

import com.sismics.reader.core.constant.BugStatus;

public class BugReportDto {
    private Long id;
    private String email;
    private String description;
    private Long timestamp;
    private BugStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
