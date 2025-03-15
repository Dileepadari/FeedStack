package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.BugReportCriteria;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.dao.jpa.mapper.BugReportMapper;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import javax.persistence.EntityManager;

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
    public Long createBugReport(BugReportDto bugReportDto) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(
                "INSERT INTO T_BUG_REPORT (BUG_ID_C, BUG_EMAIL_C, BUG_DESCRIPTION_C, BUG_TIMESTAMP_D, BUG_STATUS_C) VALUES (?, ?, ?, ?, ?)");
        q.setParameter(1, bugReportDto.getId());
        q.setParameter(2, bugReportDto.getEmail());
        q.setParameter(3, bugReportDto.getDescription());
        q.setParameter(4, bugReportDto.getTimestamp());
        q.setParameter(5, bugReportDto.getStatus());
        if (q.executeUpdate() != 1) {
            throw new RuntimeException("Failed to create bug report");
        }

        return bugReportDto.getId();
    }

}