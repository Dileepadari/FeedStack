package com.sismics.reader.core.model.jpa;

import com.sismics.reader.core.constant.BugStatus;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.EnumType;
import java.util.Date;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "BUG_STATUS_C", nullable = false)
    private BugStatus status;

    // Default constructor for JPA
    public BugReport() {
    }

    // Private constructor for Builder
    private BugReport(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.description = builder.description;
        this.timestamp = builder.timestamp;
        this.status = builder.status;
    }

    // Getters and setters with validation
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        this.timestamp = timestamp;
    }

    public BugStatus getStatus() {
        return status;
    }

    public void setStatus(BugStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    // Builder class
    public static class Builder {
        private String id;
        private String email;
        private String description;
        private Date timestamp;
        private BugStatus status;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setStatus(BugStatus status) {
            this.status = status;
            return this;
        }

        public BugReport build() {
            return new BugReport(this);
        }
    }
}
