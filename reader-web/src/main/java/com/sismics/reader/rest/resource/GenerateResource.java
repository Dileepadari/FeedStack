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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST API to retrieve and summarize articles using LLM (Groq API).
 */
@Path("/generate")
public class GenerateResource extends BaseResource {
    private static final Logger LOGGER = Logger.getLogger(GenerateResource.class.getName());
    private final int max_articles = 10;
    private final SummarizationStrategy articlesummary;
    private final SummarizationStrategy reportsummary;

    public GenerateResource() {
        this.articlesummary = new ArticleSummary(); // Using the ArticleSummary class
        this.reportsummary = new ReportSummary();
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

        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria criteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);


        Calendar cal = Calendar.getInstance();
        long todayInMillis = cal.getTimeInMillis();
        if (afterArticle != null) {
            UserArticleDto lastArticle = getLastArticle(userArticleDao, afterArticle);
            
            criteria.setArticlePublicationDateMax(new Date(lastArticle.getArticlePublicationTimestamp()));
            criteria.setArticleIdMax(lastArticle.getArticle().getId());
        }
        else if(afterArticle == null){
            JSONObject empty = new JSONObject();
            empty.put("articles", new JSONArray());
            // return Response.ok().entity({"articles":[]}).build();
        }
        
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(100, null);
        userArticleDao.findByCriteria(paginatedList, criteria, null, null);
        JSONObject result = new JSONObject();
        result = buildSummarizedResponse(paginatedList,todayInMillis);
        LOGGER.info("\n\n\n\n\n\n\n\n\n\n\n\n\n"+result+"\n\n\n\n\n\n\n\n\n\n");
        return Response.ok().entity(result).build();
    }

    private UserArticleDto getLastArticle(UserArticleDao userArticleDao, String afterArticle) {
        List<UserArticleDto> articles = userArticleDao.findByCriteria(
                new UserArticleCriteria().setUserArticleId(afterArticle).setUserId(principal.getId())
        );

        return articles.get(0);
    }

    private JSONObject buildSummarizedResponse(PaginatedList<UserArticleDto> articles,long todayInMillis) throws JSONException {
        JSONObject response = new JSONObject();
        List<JSONObject> summarizedArticles = new ArrayList<>();
        StringBuilder completeSummary = new StringBuilder();
        String separator = "@)@%";
        UserArticleDto userReport = null;

        for (UserArticleDto article : articles.getResultList()) {
            double weight = calculateCategoryWeight(article);
            String content = Optional.ofNullable(article.getArticle().getDescription()).orElse(article.getArticle().getTitle());
            String summary = articlesummary.summarize(content, weight);
            completeSummary.append(separator).append("\n").append(article.getFeedTitle()).append("\n").append(summary).append("\n");
            userReport = article;
            Date publicationDate = new Date(article.getArticlePublicationTimestamp());
            LOGGER.info("\n\n\n\n\nProcessing article published on: " + publicationDate+"\n\n\n\n");
        }

        String finalSummary = refineSummary(completeSummary.toString(), separator);
        finalSummary = reportsummary.summarize(finalSummary,1);

        if (userReport != null) {
            JSONObject articleJson = ArticleAssembler.asJson(userReport);
            
            // Get the subscription object
            JSONObject subscription = articleJson.getJSONObject("subscription");
        
            // Swap the title
            articleJson.put("title","User Daily Report"); 
            subscription.put("title","RSS-Reader");        
            // Set other required fields
            articleJson.put("url", "null");
            articleJson.put("creator", "Team-20");
            articleJson.put("date",todayInMillis);
            articleJson.put("description", finalSummary);
        
            summarizedArticles.add(articleJson);
        }
        
        response.put("articles", summarizedArticles);
        return response;
    }

    private double calculateCategoryWeight(UserArticleDto article) {
        double weight = 1.0;
        try {
            List<FeedSubscriptionDto> subscriptions = new FeedSubscriptionDao().findByCriteria(
                    new FeedSubscriptionCriteria().setFeedId(article.getArticle().getFeedId()).setUserId(principal.getId())
            );
            for (FeedSubscriptionDto subscription : subscriptions) {
                if (subscription.getCategory() != null && subscription.getCategory().getParentId() != null &&
                        subscription.getId().equals(article.getFeedSubscriptionId())) {
                    weight = 0.7;
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking category information", e);
        }
        return weight;
    }

    private String refineSummary(String summary, String separator) {
        int count = 0, index = -1;
        for (int i = 0; i < summary.length(); i++) {
            if (summary.startsWith(separator, i)) {
                count++;
                if (count == this.max_articles) {
                    index = i;
                    break;
                }
            }
        }
        return (index != -1 ? summary.substring(0, index) : summary).replace(separator, "");
    }
}
