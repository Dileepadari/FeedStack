package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.BugReportCriteria;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.dao.jpa.mapper.BugReportMapper;
import java.util.logging.Logger;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;
import com.sismics.reader.core.constant.BugStatus;
import com.sismics.reader.core.model.jpa.BugReport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

public class BugReportDao extends BaseDao<BugReport, BugReportCriteria> {
    private static final Logger logger = Logger.getLogger(BugReportDao.class.getName());
    @PersistenceContext
    private EntityManager entityManager;

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected QueryParam getQueryParam(BugReportCriteria criteria, FilterCriteria filterCriteria) {
        StringBuilder sb = new StringBuilder("SELECT b FROM BugReport b WHERE 1=1");
        List<String> criteriaList = new ArrayList<>();
        Map<String, Object> parameterMap = new HashMap<>();

        if (criteria.getEmail() != null) {
            sb.append(" AND b.email = :email");
            criteriaList.add("email");
            parameterMap.put("email", criteria.getEmail());
        }
        if (criteria.getStatus() != null) {
            sb.append(" AND b.status = :status");
            criteriaList.add("status");
            parameterMap.put("status", criteria.getStatus());
        }
        if (criteria.getDescription() != null) {
            sb.append(" AND b.description = :description");
            criteriaList.add("description");
            parameterMap.put("description", criteria.getDescription());
        }

        TypedQuery<BugReport> query = getEntityManager().createQuery(sb.toString(), BugReport.class);

        if (criteria.getEmail() != null) {
            query.setParameter("email", criteria.getEmail());
        }
        if (criteria.getStatus() != null) {
            query.setParameter("status", criteria.getStatus());
        }
        if (criteria.getDescription() != null) {
            query.setParameter("description", criteria.getDescription());
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria,
                new BugReportMapper());
    }

    public String createBugReport(BugReport bugReport) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            em.persist(bugReport);
            return bugReport.getId();
        } catch (PersistenceException e) {
            logger.severe("Failed to create bug report: " + e.getMessage());
            throw new RuntimeException("Failed to create bug report", e);
        }
    }

    public void updateBugReportStatus(String id, BugStatus status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            BugReport bugReport = em.find(BugReport.class, id);
            if (bugReport != null) {
                bugReport.setStatus(status);
                em.merge(bugReport);
            } else {
                throw new RuntimeException("Bug report not found for ID: " + id);
            }
        } catch (PersistenceException e) {
            logger.severe("Failed to update bug report status: " + e.getMessage());
            throw new RuntimeException("Failed to update bug report status", e);
        }
    }

    public void deleteBugReport(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            BugReport bugReport = em.find(BugReport.class, id);
            if (bugReport != null) {
                em.remove(bugReport);
            } else {
                throw new RuntimeException("Bug report not found for ID: " + id);
            }
        } catch (PersistenceException e) {
            logger.severe("Failed to delete bug report: " + e.getMessage());
            throw new RuntimeException("Failed to delete bug report", e);
        }
    }

    public BugReportDto getBugReport(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        BugReport bugReport = em.find(BugReport.class, id);
        if (bugReport != null) {
            return new BugReportMapper().map(bugReport);
        } else {
            throw new RuntimeException("Bug report not found for ID: " + id);
        }
    }

    public List<BugReportDto> getAllBugReports() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<BugReport> query = em.createQuery("SELECT b FROM BugReport b", BugReport.class);
        List<BugReport> bugReports = query.getResultList();
        return new BugReportMapper().mapBugReports(bugReports);
    }

    public List<BugReportDto> getBugReportsByStatus(String status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<BugReport> query = em.createQuery("SELECT b FROM BugReport b WHERE b.status = :status",
                BugReport.class);
        query.setParameter("status", BugStatus.valueOf(status));
        List<BugReport> bugReports = query.getResultList();
        return new BugReportMapper().mapBugReports(bugReports);
    }

    public List<BugReportDto> getBugReportsByEmail(String email) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<BugReport> query = em.createQuery("SELECT b FROM BugReport b WHERE b.email = :email",
                BugReport.class);
        query.setParameter("email", email);
        List<BugReport> bugReports = query.getResultList();
        return new BugReportMapper().mapBugReports(bugReports);
    }
}
