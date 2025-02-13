```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedMapper;
import com.sismics.reader.core.model.jpa.Feed;
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
 * Feed DAO.
 *
 * @author jtremeaux
 */
public class FeedDao extends BaseDao<FeedDto, FeedCriteria> {

    @Override
    protected QueryParam getQueryParam(FeedCriteria criteria, FilterCriteria filterCriteria) {
        StringBuilder sb = new StringBuilder("select f from Feed f where f.deleteDate is null");
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        if (criteria.getFeedUrl() != null) {
            criteriaList.add("f.rssUrl = :feedUrl");
            parameterMap.put("feedUrl", criteria.getFeedUrl());
        }
        if (criteria.isWithUserSubscription() && criteria.getUserId() != null) {
            criteriaList.add("(select count(fs.id) from FeedSubscription fs where fs.feedId = f.id and fs.userId = :userId and fs.deleteDate is null) > 0");
            parameterMap.put("userId", criteria.getUserId());
        }

        SortCriteria sortCriteria = new SortCriteria(" order by f.createDate asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new FeedMapper());
    }

    /**
     * Creates a new feed.
     *
     * @param feed Feed to create
     * @return New ID
     */
    public String create(Feed feed) {
        feed.setId(UUID.randomUUID().toString());
        feed.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(feed);
        return feed.getId();
    }

    /**
     * Deletes a feed.
     *
     * @param id Feed ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null")
                .setParameter("id", id);
        Feed feedFromDb = (Feed) q.getSingleResult();
        feedFromDb.setDeleteDate(new Date());
    }

    /**
     * Get an active feed by its URL.
     *
     * @param rssUrl RSS URL
     */
    public Feed getByRssUrl(String rssUrl) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return (Feed) em.createQuery("select f from Feed f where f.rssUrl = :rssUrl and f.deleteDate is null")
                    .setParameter("rssUrl", rssUrl)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Updates a feed.
     *
     * @param feed Feed to update
     * @return Updated feed
     */
    public Feed update(Feed feed) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f from Feed f where f.id = :id and f.deleteDate is null")
                .setParameter("id", feed.getId());
        Feed feedFromDb = (Feed) q.getSingleResult();

        feedFromDb.setUrl(feed.getUrl());
        feedFromDb.setBaseUri(feed.getBaseUri());
        feedFromDb.setTitle(feed.getTitle());
        feedFromDb.setLanguage(feed.getLanguage());
        feedFromDb.setDescription(feed.getDescription());
        feedFromDb.setLastFetchDate(feed.getLastFetchDate());

        return feedFromDb;
    }
}