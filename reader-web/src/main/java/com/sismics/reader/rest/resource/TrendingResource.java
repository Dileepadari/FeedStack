package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.service.TrendingArticleService;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Trending articles REST resource.
 * 
 * @author [your name]
 */
@Path("/trending")
public class TrendingResource extends BaseResource {
    /**
     * Returns trending articles.
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get trending articles
        List<ArticleDto> trendingArticles = TrendingArticleService.getInstance().getTrendingArticles();
        
        // Log trending articles to server console
        System.out.println("=== TOP 5 TRENDING ARTICLES ===");
        for (int i = 0; i < trendingArticles.size(); i++) {
            ArticleDto article = trendingArticles.get(i);
            System.out.println((i+1) + ". " + article.getTitle() + " - " + article.getStarCount() + " stars");
        }
        System.out.println("================================");
        
        // Build the response
        JSONObject response = new JSONObject();
        JSONArray articles = new JSONArray();
        
        for (ArticleDto article : trendingArticles) {
            JSONObject json = new JSONObject();
            json.put("id", article.getId());
            json.put("title", article.getTitle());
            json.put("url", article.getUrl());
            json.put("creator", article.getCreator());
            json.put("star_count", article.getStarCount());
            articles.put(json);
        }
        
        response.put("articles", articles);
        return Response.ok().entity(response).build();
    }
} 