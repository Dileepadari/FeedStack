package com.sismics.reader.rest.resource;
import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


abstract class CategoryBaseResource extends ArticleManagementBaseResource {
    protected CategoryDao categoryDao;

    public CategoryBaseResource() {
        super();
        this.categoryDao = new CategoryDao();
    }

    protected Category validateCategory(String categoryId) throws JSONException {
        try {
            return categoryDao.getCategory(categoryId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound",
                    MessageFormat.format("Category not found: {0}", categoryId));
        }
    }

    protected void markCategoryArticlesAsRead(String categoryId) {
        userArticleDao.markAsRead(new UserArticleCriteria()
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setCategoryId(categoryId));

        for (FeedSubscriptionDto feedSubscription : feedSubscriptionDao.findByCriteria(
                new FeedSubscriptionCriteria()
                        .setCategoryId(categoryId)
                        .setUserId(principal.getId()))) {
            feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), 0);
        }
    }

    protected JSONObject buildCategoryJson(Category category) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", category.getId());
        json.put("name", category.getName());
        if (category.getParentId() != null) {
            json.put("folded", category.isFolded());
        }
        return json;
    }


    protected JSONObject buildSubscriptionJson(FeedSubscriptionDto subscription) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", subscription.getId());
        json.put("title", subscription.getFeedSubscriptionTitle());
        json.put("url", subscription.getFeedRssUrl());
        json.put("unread_count", subscription.getUnreadUserArticleCount());
        return json;
    }

    protected Response buildSubscriptionResponse(
            FeedSubscriptionDto subscription,
            PaginatedList<UserArticleDto> articles) throws JSONException {

        JSONObject response = new JSONObject();
        JSONObject subscriptionJson = new JSONObject();

        subscriptionJson.put("title", subscription.getFeedSubscriptionTitle());
        subscriptionJson.put("feed_title", subscription.getFeedTitle());
        subscriptionJson.put("url", subscription.getFeedUrl());
        subscriptionJson.put("rss_url", subscription.getFeedRssUrl());
        subscriptionJson.put("description", subscription.getFeedDescription());

        response.put("subscription", subscriptionJson);
        response.put("articles", buildArticleJsonArray(articles));

        return Response.ok().entity(response).build();
    }

    protected JSONArray buildArticleJsonArray(PaginatedList<UserArticleDto> articles) throws JSONException {
        JSONArray articlesJson = new JSONArray();
        for (UserArticleDto article : articles.getResultList()) {
            articlesJson.put(ArticleAssembler.asJson(article));
        }
        return articlesJson;
    }

    protected Response buildSubscriptionListResponse(
            List<FeedSubscriptionDto> subscriptions,
            Category rootCategory,
            boolean unread) throws JSONException {

        JSONObject response = new JSONObject();
        JSONObject rootCategoryJson = buildCategoryJson(rootCategory);
        List<JSONObject> rootCategories = new ArrayList<>();
        rootCategories.add(rootCategoryJson);

        String oldCategoryId = null;
        JSONObject categoryJson = rootCategoryJson;
        int totalUnreadCount = 0;

        for (FeedSubscriptionDto subscription : subscriptions) {
            if (!subscription.getCategoryId().equals(oldCategoryId)) {
                if (subscription.getCategoryParentId() != null) {
                    categoryJson = new JSONObject();
                    categoryJson.put("id", subscription.getCategoryId());
                    categoryJson.put("name", subscription.getCategoryName());
                    categoryJson.put("folded", subscription.isCategoryFolded());
                    categoryJson.put("subscriptions", new JSONArray());
                }
            }

            JSONObject subscriptionJson = buildSubscriptionJson(subscription);
            ((JSONArray) categoryJson.get("subscriptions")).put(subscriptionJson);

            oldCategoryId = subscription.getCategoryId();
            totalUnreadCount += subscription.getUnreadUserArticleCount();
        }

        response.put("categories", rootCategories);
        response.put("unread_count", totalUnreadCount);
        return Response.ok().entity(response).build();
    }
    protected FeedSubscriptionDto validateAndGetSubscription(String id) throws JSONException {
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setId(id)
                .setUserId(principal.getId());

        List<FeedSubscriptionDto> subscriptions = feedSubscriptionDao.findByCriteria(criteria);
        if (subscriptions.isEmpty()) {
            throw new ClientException("SubscriptionNotFound",
                    MessageFormat.format("Subscription not found: {0}", id));
        }
        return subscriptions.iterator().next();
    }
    protected boolean isAlreadySubscribed(String url) {
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setUserId(principal.getId())
                .setFeedUrl(url);
        return !feedSubscriptionDao.findByCriteria(criteria).isEmpty();
    }


    protected FeedSubscription createSubscription(Feed feed, String title) {
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        Integer displayOrder = feedSubscriptionDao.getCategoryCount(rootCategory.getId(), principal.getId());

        FeedSubscription subscription = new FeedSubscription();
        subscription.setUserId(principal.getId());
        subscription.setFeedId(feed.getId());
        subscription.setCategoryId(rootCategory.getId());
        subscription.setOrder(displayOrder);
        subscription.setUnreadCount(0);
        subscription.setTitle(title);

        return subscription;
    }
}