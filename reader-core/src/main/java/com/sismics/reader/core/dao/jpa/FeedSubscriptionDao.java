package com.sismics.reader.core.dao.jpa;

import com.google.common.collect.Lists;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedSubscriptionMapper;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * Feed subscription DAO.
 *
 * @author jtremeaux
 */
public class FeedSubscriptionDao extends BaseDao<FeedSubscriptionDto, FeedSubscriptionCriteria> {

    @Override
    protected QueryParam getQueryParam(FeedSubscriptionCriteria criteria, FilterCriteria filterCriteria) {
        StringBuilder sb = new StringBuilder("select fs from FeedSubscription fs " +
                "join Feed f on f.id = fs.feedId and f.deleteDate is null " +
                "join Category c on c.id = fs.categoryId and c.deleteDate is null " +
                "where fs.deleteDate is null");
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = Lists.newArrayList();

        if (criteria.getId() != null) {
            criteriaList.add("fs.id = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("fs.userId = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("fs.feedId = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        if (criteria.getCategoryId() != null) {
            criteriaList.add("fs.categoryId = :categoryId");
            parameterMap.put("categoryId", criteria.getCategoryId());
        }
        if (criteria.getFeedUrl() != null) {
            criteriaList.add("f.rssUrl = :feedUrl");
            parameterMap.put("feedUrl", criteria.getFeedUrl());
        }
        if (criteria.isUnread()) {
            criteriaList.add("fs.unreadCount > 0");
        }

        SortCriteria sortCriteria = new SortCriteria(" order by c.parentCategoryId asc, c.order asc, fs.order asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new FeedSubscriptionMapper());
    }

    /**
     * Creates a new feed subscription.
     *
     * @param feedSubscription Feed subscription to create
     * @return New ID
     */
    public String create(FeedSubscription feedSubscription) {
        feedSubscription.setId(UUID.randomUUID().toString());
        feedSubscription.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(feedSubscription);
        return feedSubscription.getId();
    }

    /**
     * Updates a feedSubscription.
     *
     * @param feedSubscription FeedSubscription
     * @return Updated feedSubscription
     */
    public FeedSubscription update(FeedSubscription feedSubscription) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.id = :id and fs.deleteDate is null")
                .setParameter("id", feedSubscription.getId());
        FeedSubscription feedSubscriptionFromDb = (FeedSubscription) q.getSingleResult();

        feedSubscriptionFromDb.setTitle(feedSubscription.getTitle());
        feedSubscriptionFromDb.setCategoryId(feedSubscription.getCategoryId());
        feedSubscriptionFromDb.setOrder(feedSubscription.getOrder());

        return feedSubscriptionFromDb;
    }

    /**
     * Update the number of unread articles in