```java
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
public class AllResource extends BaseResource {
    /**
     * Returns all articles.
     *
     * @param unread Returns only unread articles
     * @param limit  Page limit
     * @param afterArticle Start the list after this user article
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response get(
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        if (afterArticle != null) {
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticleId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        JSONObject response = new JSONObject();
        try {
            response.put("articles", articles);
        } catch (JSONException e) {
            throw new ClientException("UnexpectedError", e);
        }

        return Response.ok().entity(response).build();
    }

    /**
     * Marks all articles as read.
     *
     * @return Response
     */
    @POST
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response read() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUserId(principal.getId())
                .setSubscribed(true);
        userArticleDao.markAsRead(userArticleCriteria);

        FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria()
                .setUserId(principal.getId());
        for (FeedSubscriptionDto feedSubscrition : feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria)) {
            feedSubscriptionDao.updateUnreadCount(feedSubscrition.getId(), 0);
        }

        JSONObject response = new JSONObject();
        try {
            response.put("status", "ok");
        } catch (JSONException e) {
            throw new ClientException("UnexpectedError", e);
        }
        return Response.ok().entity(response).build();
    }
}
```
```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.reader.core.util.ConfigUtil;
import com.sismics.reader.core.util.NetworkUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.NetworkUtil;
import com.sismics.util.log4j.LogCriteria;
import com.sismics.util.log4j.LogEntry;
import com.sismics.util.log4j.MemoryAppender;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * General app REST resource.
 *
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
    private static final Logger LOGGER = Logger.getLogger(AppResource.class);

    /**
     * Return the information about the application.
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response version() throws JSONException {
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JSONObject response = new JSONObject();
        response.put("current_version", currentVersion.replace("-SNAPSHOT", ""));
        response.put("min_version", minVersion);
        response.put("total_memory", Runtime.getRuntime().totalMemory());
        response.put("free_memory", Runtime.getRuntime().freeMemory());
        return Response.ok().entity(response).build();
    }

    /**
     * Retrieve the application logs.
     *
     * @param level   Filter on logging level
     * @param tag     Filter on logger name / tag
     * @param message Filter on message
     * @param limit   Page limit
     * @param offset  Page offset
     * @return Response
     */
    @GET
    @Path("log")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response log(@QueryParam("level") String level,
                        @QueryParam("tag") String tag,
                        @QueryParam("message") String message,
                        @QueryParam("limit") Integer limit,
                        @QueryParam("offset") Integer offset) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        MemoryAppender memoryAppender = getMemoryAppender();

        LogCriteria logCriteria = new LogCriteria()
                .setLevel(StringUtils.stripToNull(level))
                .setTag(StringUtils.stripToNull(tag))
                .setMessage(StringUtils.stripToNull(message));

        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
        memoryAppender.find(logCriteria, paginatedList);
        JSONObject response = new JSONObject();
        List<JSONObject> logs = new ArrayList<>();
        for (LogEntry logEntry : paginatedList.getResultList()) {
            JSONObject log = new JSONObject();
            log.put("date", logEntry.getTimestamp());
            log.put("level",