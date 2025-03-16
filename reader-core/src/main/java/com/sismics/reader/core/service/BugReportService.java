package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.jpa.BugReportDao;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.model.jpa.BugReport;
import com.sismics.reader.core.constant.BugStatus;

import java.util.Date;
import java.util.List;

public class BugReportService {
    private BugReportDao bugReportDao;

    public BugReportService() {
        this.bugReportDao = new BugReportDao();
    }

    public String createBugReport(String email, String description) {
        BugReport bugReport = new BugReport();
        bugReport.setId(java.util.UUID.randomUUID().toString());
        bugReport.setEmail(email);
        bugReport.setDescription(description);
        bugReport.setTimestamp(new Date());
        bugReport.setStatus(BugStatus.OPEN);
        return bugReportDao.createBugReport(bugReport);
    }

    public void updateBugReportStatus(String id, BugStatus status) {
        bugReportDao.updateBugReportStatus(id, status);
    }

    public void deleteBugReport(String id) {
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
