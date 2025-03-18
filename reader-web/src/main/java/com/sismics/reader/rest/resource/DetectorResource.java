package com.sismics.reader.rest.resource;
import java.text.MessageFormat;
import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;

@Path("/detector")
public class DetectorResource extends BaseResource {
    private DetectorCommand detectorCommand;

    public DetectorResource() {
        this.detectorCommand = new PythonDetectorCommand(); // Default implementation
    }

    public void setDetectorCommand(DetectorCommand command) {
        this.detectorCommand = command;
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
        double threshold = 0.6;
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        limit = 100;

        if (afterArticle != null) {
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound",
                        MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticle().getId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);

        List<JSONObject> articles = new ArrayList<>();
        List<String> articleIds = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
            articleIds.add(userArticle.getId());
            titles.add(userArticle.getArticle().getTitle());
            descriptions.add(userArticle.getArticle().getDescription());
        }

        String pythonResponse = detectorCommand.execute(articleIds, titles, descriptions,threshold);

        JSONObject response = new JSONObject();
        response.put("articles", articles);
        response.put("duplicates", pythonResponse != null ? new JSONObject(pythonResponse) : new JSONObject());

        JSONObject duplicates = response.getJSONObject("duplicates");
        List<JSONObject> filteredArticles = filterDuplicates(articles, duplicates);
        response.put("articles", filteredArticles);
        List<JSONObject> markedArticles = markDuplicates(articles, duplicates);
        response.put("articles", markedArticles);

        System.out.println("\n\n\n\nResponse JSON:\n" + response.toString(4) + "\n\n\n\n");
        return Response.ok().entity(response).build();
    }

    private List<JSONObject> filterDuplicates(List<JSONObject> articles, JSONObject duplicates) {
        List<JSONObject> filteredArticles = new ArrayList<>();
        JSONArray duplicateIds = duplicates.optJSONArray("duplicates");
        Set<String> duplicateIdSet = new HashSet<>();  // Use HashSet for O(1) lookups

        if (duplicateIds != null) {
            for (int i = 0; i < duplicateIds.length(); i++) {
                JSONArray pair = duplicateIds.optJSONArray(i); // Avoid exceptions
                if (pair != null) {
                    for (int j = 0; j < pair.length(); j++) {
                        duplicateIdSet.add(pair.optString(j)); // Use optString to avoid JSONException
                    }
                }
            }
        }

        System.out.println("Duplicate IDs: " + duplicateIdSet);

        for (JSONObject article : articles) {
            if (!duplicateIdSet.contains(article.optString("id"))) {
                filteredArticles.add(article);
            }
        }
        return filteredArticles;
    }

    private List<JSONObject> markDuplicates(List<JSONObject> articles, JSONObject duplicates) {
        Set<String> duplicateIdSet = new HashSet<>();
        JSONArray duplicateIds = duplicates.optJSONArray("duplicates");
    
        if (duplicateIds != null) {
            for (int i = 0; i < duplicateIds.length(); i++) {
                JSONArray pair = duplicateIds.optJSONArray(i);
                if (pair != null) {
                    for (int j = 0; j < pair.length(); j++) {
                        duplicateIdSet.add(pair.optString(j));
                    }
                }
            }
        }
    
        System.out.println("Duplicate IDs: " + duplicateIdSet);
    
        for (JSONObject article : articles) {
            try {
                String articleId = article.optString("id");
                boolean isDuplicate = duplicateIdSet.contains(articleId);
                article.put("duplicate", isDuplicate ? "yes" : "no");
            } catch (JSONException e) {
                e.printStackTrace(); // Log the error
            }
        }
    
        return articles;
    }
    

}
