package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
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
 * All articles REST resources.
 * 
 * @author jtremeaux
 */
@Path("/all")
public class AllResource extends ArticleManagementBaseResource {
    /**
     * Returns all articles.
     * 
     * @param unread       Returns only unread articles
     * @param limit        Page limit
     * @param afterArticle Start the list after this user article
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
//        if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
            validateAuthentication();
        // Get the articles
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);

        PaginatedList<UserArticleDto> paginatedList =getPaginatedArticles(userArticleCriteria,limit,afterArticle);
      
        return buildArticleListResponse(paginatedList);
    }

    /**
     * Marks all articles as read.
     * 
     * @return Response
     */
    @POST
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read() throws JSONException {

        validateAuthentication();

        // Marks all articles of this user as read
        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.markAsRead(new UserArticleCriteria()
                .setUserId(principal.getId())
                .setSubscribed(true));

        updateFeedSubscriptionUnreadCount(principal.getId(),0);

        return Response.ok().entity(buildOkResponse()).build();
    }

}
