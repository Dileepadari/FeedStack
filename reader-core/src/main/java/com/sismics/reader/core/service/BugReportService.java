package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.jpa.BugReportDao;
import java.util.logging.Logger;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.model.jpa.BugReport;
import com.sismics.reader.core.constant.BugStatus;

import java.util.Date;
import java.util.List;

public class BugReportService {
    private BugReportDao bugReportDao;
    private static final Logger logger = Logger.getLogger(BugReportService.class.getName());

    public BugReportService() {
        this.bugReportDao = new BugReportDao();
    }

    public String createBugReport(String email, String description) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            logger.warning("Invalid email provided: " + email);
            throw new IllegalArgumentException("Invalid email");
        }
        if (description == null || description.isEmpty()) {
            logger.warning("Empty description provided");
            throw new IllegalArgumentException("Description cannot be empty");
        }

        BugReport bugReport = new BugReport();
        bugReport.setId(java.util.UUID.randomUUID().toString());
        bugReport.setEmail(email);
        bugReport.setDescription(description);
        bugReport.setTimestamp(new Date());
        bugReport.setStatus(BugStatus.OPEN);
        logger.info("Creating bug report: " + bugReport);
        return bugReportDao.createBugReport(bugReport);
    }

    public void updateBugReportStatus(String id, BugStatus status) {
        logger.info("Updating bug report status for ID: " + id + " to " + status);
        bugReportDao.updateBugReportStatus(id, status);
    }

    public void deleteBugReport(String id) {
        logger.info("Deleting bug report with ID: " + id);
        bugReportDao.deleteBugReport(id);
    }

    public BugReportDto getBugReport(String id) {
        return bugReportDao.getBugReport(id);
    }

    public List<BugReportDto> getAllBugReports() {
        return bugReportDao.getAllBugReports();
    }

    public List<BugReportDto> getBugReportsByEmail(String email) {
        return bugReportDao.getBugReportsByEmail(email);
    }
}
