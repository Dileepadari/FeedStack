package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.*;
import com.sismics.reader.core.dao.jpa.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API to retrieve articles and summarize them using LLM (Groq API).
 */
@Path("/articlesummary")
public class ArticleSummaryResource extends BaseResource {

    private static final Logger LOGGER = Logger.getLogger(ArticleSummaryResource.class.getName());
    private final SummarizationStrategy summarizationStrategy;

    public ArticleSummaryResource() {
        this.summarizationStrategy = new ArticleSummary(); // Using the ArticleSummary class
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        List<UserArticleDto> articles = fetchArticles(unread, limit, afterArticle);
        List<JSONObject> processedArticles = processArticles(articles);

        JSONObject response = new JSONObject();
        response.put("articles", processedArticles);
        
        return Response.ok().entity(response).build();
    }

    private List<UserArticleDto> fetchArticles(boolean unread, Integer limit, String afterArticle) {
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);

        if (afterArticle != null) {
            UserArticleDto userArticleDto = findAfterArticle(afterArticle, userArticleDao);
            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticle().getId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);
        return paginatedList.getResultList();
    }

    private UserArticleDto findAfterArticle(String afterArticle, UserArticleDao userArticleDao) {
        UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                .setUserArticleId(afterArticle)
                .setUserId(principal.getId());

        List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);

        return userArticleDtoList.iterator().next();
    }

    private List<JSONObject> processArticles(List<UserArticleDto> articles) throws JSONException {
        List<JSONObject> processedArticles = new ArrayList<>();
        for (UserArticleDto article : articles) {
            double weight = determineCategoryWeight(article);
            String content = article.getArticle().getTitle() + ". " + 
                             Optional.ofNullable(article.getArticle().getDescription()).orElse("");
            LOGGER.info("Summarizing article with weight " + weight + ": " + content);
            String summary = summarizationStrategy.summarize(content, weight);
            LOGGER.info("Summary: " + summary);

            JSONObject articleJson = ArticleAssembler.asJson(article);
            articleJson.put("description", summary);
            processedArticles.add(articleJson);
        }
        return processedArticles;
    }

    private double determineCategoryWeight(UserArticleDto article) {
        double weight = 1.0;
        try {
            FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
            List<FeedSubscriptionDto> subscriptions = feedSubscriptionDao.findByCriteria(
                new FeedSubscriptionCriteria()
                    .setFeedId(article.getArticle().getFeedId())
                    .setUserId(principal.getId())
            );
            for (FeedSubscriptionDto subscription : subscriptions) {
                CategoryDto category = subscription.getCategory();
                if (category != null && category.getParentId() != null 
                    && subscription.getId() == article.getFeedSubscriptionId()) {
                    weight = 0.7;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking category information", e);
        }
        return weight;
    }
}
