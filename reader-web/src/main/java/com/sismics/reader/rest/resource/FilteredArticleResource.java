package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
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

/**
 * Filtered articles REST resources.
 * 
 * @author [your name]
 */
@Path("/filter")
public class FilteredArticleResource extends BaseResource {

    /**
     * Returns articles filtered by categories and/or sources.
     * 
     * @param categoryIds  List of category IDs to filter by
     * @param sourceIds    List of subscription IDs to filter by
     * @param unread       Returns only unread articles
     * @param limit        Page limitLorem
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("category") List<String> categoryIds,
            @QueryParam("source") List<String> sourceIds,
            @QueryParam("filter") String filter,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Create a filter strategy based on provided parameters
        ArticleFilterStrategy filterStrategy;
        if (!categoryIds.isEmpty() && !sourceIds.isEmpty()) {
            filterStrategy = new CombinedFilterStrategy(categoryIds, sourceIds);
        } else if (!categoryIds.isEmpty()) {
            filterStrategy = new CategoryFilterStrategy(categoryIds);
        } else if (!sourceIds.isEmpty()) {
            filterStrategy = new SourceFilterStrategy(sourceIds);
        } else {
            // Default to all articles if no filters specified
            filterStrategy = new DefaultFilterStrategy();
        }

        // Build the criteria using the selected strategy
        UserArticleCriteriaBuilder criteriaBuilder = new UserArticleCriteriaBuilder()
                .withUserId(principal.getId())
                .withSubscribed(true)
                .withVisible(true);

        boolean unread = filter.equalsIgnoreCase("unread");
        boolean starred = filter.equalsIgnoreCase("starred");
        if (unread) {
            criteriaBuilder.withUnread(true);
        } else if (starred) {
            criteriaBuilder.withStarred(true);
        }

        // Apply the filter strategy
        UserArticleCriteria userArticleCriteria = filterStrategy.applyCriteria(criteriaBuilder);

        // Handle pagination
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleDao userArticleDao = new UserArticleDao();

            // Create criteria to find the reference article
            UserArticleCriteriaBuilder afterCriteriaBuilder = new UserArticleCriteriaBuilder()
                    .withUserId(principal.getId());

            if (unread) {
                afterCriteriaBuilder.withUnread(true);
            } else if (starred) {
                afterCriteriaBuilder.withStarred(true);
            }

            // Apply the same filter strategy
            UserArticleCriteria afterArticleCriteria = filterStrategy.applyCriteria(afterCriteriaBuilder);
            afterArticleCriteria.setUserArticleId(afterArticle);

            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                System.out.println("Criteria for afterArticle: " + afterArticleCriteria);
                System.out.println("afterArticle: " + afterArticle);
                throw new ClientException("ArticleNotFound",
                        MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticle().getId());
        }

        // Get the articles
        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);

        System.out.println("limit: " + limit);
        System.out.println(paginatedList.toString());

        // Build the response
        JSONObject response = new JSONObject();

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }
}