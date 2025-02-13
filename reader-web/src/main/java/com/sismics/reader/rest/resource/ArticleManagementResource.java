package com.sismics.reader.rest.resource;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//public abstract class ArticleManagementResource extends BaseResource {
abstract class ArticleManagementBaseResource extends BaseResource{
        protected UserArticleDao userArticleDao;
        protected FeedSubscriptionDao feedSubscriptionDao;

        public ArticleManagementBaseResource() {
            this.userArticleDao = new UserArticleDao();
            this.feedSubscriptionDao = new FeedSubscriptionDao();
        }

        protected PaginatedList<UserArticleDto> getPaginatedArticles(
                UserArticleCriteria criteria,
                Integer limit,
                String afterArticle) throws JSONException {

            if (afterArticle != null) {
                criteria = addPaginationCriteria(criteria, afterArticle);
            }

            PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
            userArticleDao.findByCriteria(paginatedList, criteria, null, null);
            return paginatedList;
        }

        protected UserArticleCriteria addPaginationCriteria(UserArticleCriteria criteria, String afterArticle)
                throws JSONException {
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound",
                        MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            criteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            criteria.setArticleIdMax(userArticleDto.getArticleId());
            return criteria;
        }

        protected Response buildArticleListResponse(PaginatedList<UserArticleDto> paginatedList) throws JSONException {
            JSONObject response = new JSONObject();
            List<JSONObject> articles = new ArrayList<>();

            for (UserArticleDto userArticle : paginatedList.getResultList()) {
                articles.add(ArticleAssembler.asJson(userArticle));
            }
            response.put("articles", articles);

            return Response.ok().entity(response).build();
        }

        protected void updateFeedSubscriptionUnreadCount(String feedId, int delta) {
        for (FeedSubscriptionDto feedSubscription : feedSubscriptionDao.findByCriteria(
                new FeedSubscriptionCriteria()
                        .setFeedId(feedId)
                        .setUserId(principal.getId()))) {
            feedSubscriptionDao.updateUnreadCount(
                    feedSubscription.getId(),
                    feedSubscription.getUnreadUserArticleCount() + delta);
        }
    }
        protected void updateReadStatus(String articleId, boolean markAsRead) throws JSONException {
            UserArticle userArticle = validateArticle(articleId);

            if ((markAsRead && userArticle.getReadDate() == null) ||
                    (!markAsRead && userArticle.getReadDate() != null)) {

                userArticle.setReadDate(markAsRead ? new Date() : null);
                userArticleDao.update(userArticle);

                ArticleDto article = new ArticleDao().findFirstByCriteria(
                        new ArticleCriteria().setId(userArticle.getArticleId()));

                updateFeedSubscriptionUnreadCount(article.getFeedId(), markAsRead ? -1 : 1);
            }
        }

        protected UserArticle validateArticle(String articleId) throws JSONException {
            UserArticle userArticle = userArticleDao.getUserArticle(articleId, principal.getId());
            if (userArticle == null) {
                throw new ClientException("ArticleNotFound",
                        MessageFormat.format("Article not found: {0}", articleId));
            }
            return userArticle;
        }


    }

