package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.BugReportCriteria;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.dao.jpa.mapper.BugReportMapper;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;
import com.sismics.reader.core.constant.BugStatus;
import com.sismics.reader.core.model.jpa.BugReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import javax.persistence.EntityManager;
import java.util.Date;

public class BugReportDao extends BaseDao<BugReportDto, BugReportCriteria> {
    @Override
    protected QueryParam getQueryParam(BugReportCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder(
                "select b.BUG_ID_C, b.BUG_EMAIL_C, b.BUG_DESCRIPTION_C, b.BUG_TIMESTAMP_D, b.BUG_STATUS_C ")
                .append("  from T_BUG_REPORT b ");

        // Adds search criteria
        criteriaList.add("b.BUG_STATUS_C = :status");

        if (criteria.getEmail() != null) {
            criteriaList.add("b.BUG_EMAIL_C = :email");
            parameterMap.put("email", criteria.getEmail());
        }
        if (criteria.getStatus() != null) {
            parameterMap.put("status", criteria.getStatus());
        }
        if (criteria.getDescription() != null) {
            criteriaList.add("b.BUG_DESCRIPTION_C = :description");
            parameterMap.put("description", criteria.getDescription());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by b.BUG_TIMESTAMP_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria,
                new BugReportMapper());
    }

    /**
     * Creates a new BugReport
     */
    public String createBugReport(BugReport bugReport) {
        // Insert bug report into the database
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "INSERT INTO T_BUG_REPORT (BUG_ID_C, BUG_EMAIL_C, BUG_DESCRIPTION_C, BUG_TIMESTAMP_D, BUG_STATUS_C) VALUES (?, ?, ?, ?, ?)");
        q.setParameter(1, bugReport.getId());
        q.setParameter(2, bugReport.getEmail());
        q.setParameter(3, bugReport.getDescription());
        q.setParameter(4, new java.sql.Timestamp(bugReport.getTimestamp().getTime())); // Convert Date to //
        q.setParameter(5, bugReport.getStatus().name());
        if (q.executeUpdate() != 1) {
            throw new RuntimeException("Failed to create bug report");
        }

        return bugReport.getId();
    }

    /**
     * Updates a bug report
     */
    public void updateBugReportStatus(String id, BugStatus status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("UPDATE T_BUG_REPORT SET BUG_STATUS_C = ? WHERE BUG_ID_C = ?");
        q.setParameter(1, status.name());
        q.setParameter(2, id);
        if (q.executeUpdate() != 1) {
            throw new RuntimeException("Failed to update bug report");
        }
    }

    /**
     * Deletes a bug report
     */
    public void deleteBugReport(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("DELETE FROM T_BUG_REPORT WHERE BUG_ID_C =?");
        q.setParameter(1, id);
        if (q.executeUpdate() != 1) {
            throw new RuntimeException("Failed to delete bug report");
        }
    }

    /**
     * Get a bug report by id
     */
    public BugReportDto getBugReport(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "SELECT b.BUG_ID_C, b.BUG_EMAIL_C, b.BUG_DESCRIPTION_C, b.BUG_TIMESTAMP_D, b.BUG_STATUS_C " +
                        "  FROM T_BUG_REPORT b " +
                        " WHERE b.BUG_ID_C =?");
        q.setParameter(1, id);
        Object[] result = (Object[]) q.getSingleResult();
        BugReportDto dto = new BugReportDto();
        String bugId = (String) result[0];
        dto.setId(bugId);
        String email = (String) result[1];
        dto.setEmail(email);
        String description = (String) result[2];
        dto.setDescription(description);
        Date timestamp = (Date) result[3];
        dto.setTimestamp(timestamp);
        BugStatus status = BugStatus.valueOf((String) result[4]);
        dto.setStatus(status);
        return dto;
    }

    /**
     * Get all bug reports
     */
    public List<BugReportDto> getAllBugReports() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "SELECT b.BUG_ID_C, b.BUG_EMAIL_C, b.BUG_DESCRIPTION_C, b.BUG_TIMESTAMP_D, b.BUG_STATUS_C " +
                        "  FROM T_BUG_REPORT b");
        List<Object[]> results = q.getResultList();
        List<BugReportDto> bugReportDtos = new ArrayList<>();
        for (Object[] result : results) {
            BugReportDto bugReportDto = new BugReportDto();
            String id = (String) result[0];
            bugReportDto.setId(id);
            String email = (String) result[1];
            bugReportDto.setEmail(email);
            String description = (String) result[2];
            bugReportDto.setDescription(description);
            Date timestamp = (Date) result[3];
            bugReportDto.setTimestamp(timestamp);
            BugStatus status = BugStatus.valueOf((String) result[4]);
            bugReportDto.setStatus(status);
            bugReportDtos.add(bugReportDto);
        }
        return bugReportDtos;
    }

    /**
     * Get all bug reports by status
     */
    public List<BugReportDto> getBugReportsByStatus(String status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "SELECT b.BUG_ID_C, b.BUG_EMAIL_C, b.BUG_DESCRIPTION_C, b.BUG_TIMESTAMP_D, b.BUG_STATUS_C " +
                        "  FROM T_BUG_REPORT b " +
                        " WHERE b.BUG_STATUS_C =?");
        q.setParameter(1, status);
        List<Object[]> results = q.getResultList();
        List<BugReportDto> bugReportDtos = new ArrayList<>();
        for (Object[] result : results) {
            BugReportDto bugReportDto = new BugReportDto();
            String id = (String) result[0];
            bugReportDto.setId(id);
            String email = (String) result[1];
            bugReportDto.setEmail(email);
            String description = (String) result[2];
            bugReportDto.setDescription(description);
            Date timestamp = (Date) result[3];
            bugReportDto.setTimestamp(timestamp);
            BugStatus _status = BugStatus.valueOf((String) result[4]);
            bugReportDto.setStatus(_status);
            bugReportDtos.add(bugReportDto);
        }
        return bugReportDtos;
    }

    /**
     * Get all bug reports by email
     */
    public List<BugReportDto> getBugReportsByEmail(String email) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "SELECT b.BUG_ID_C, b.BUG_EMAIL_C, b.BUG_DESCRIPTION_C, b.BUG_TIMESTAMP_D, b.BUG_STATUS_C " +
                        "  FROM T_BUG_REPORT b " +
                        " WHERE b.BUG_EMAIL_C =?");
        q.setParameter(1, email);
        List<Object[]> results = q.getResultList();
        List<BugReportDto> bugReportDtos = new ArrayList<>();
        for (Object[] result : results) {
            BugReportDto bugReportDto = new BugReportDto();
            String id = (String) result[0];
            bugReportDto.setId(id);
            String user_email = (String) result[1];
            bugReportDto.setEmail(user_email);
            String description = (String) result[2];
            bugReportDto.setDescription(description);
            Date timestamp = (Date) result[3];
            bugReportDto.setTimestamp(timestamp);
            BugStatus status = BugStatus.valueOf((String) result[4]);
            bugReportDto.setStatus(status);
            bugReportDtos.add(bugReportDto);
        }
        return bugReportDtos;
    }
}