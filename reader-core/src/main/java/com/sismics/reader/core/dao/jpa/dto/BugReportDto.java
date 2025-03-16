package com.sismics.reader.core.dao.jpa.dto;

import com.sismics.reader.core.constant.BugStatus;
import java.util.Date;

public class BugReportDto {
    private String id;
    private String email;
    private String description;
    private Date timestamp;
    private BugStatus status;

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BugStatus getStatus() {
        return status;

    }

    public void setStatus(BugStatus status) {
        this.status = status;
    }

}
