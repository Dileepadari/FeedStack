```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.core.constant.BaseFunction;
import com.sismics.reader.core.dao.jpa.*;
import com.sismics.reader.core.dao.jpa.criteria.JobCriteria;
import com.sismics.reader.core.dao.jpa.criteria.JobEventCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobDto;
import com.sismics.reader.core.dao.jpa.dto.JobEventDto;
import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.event.PasswordChangedEvent;
import com.sismics.reader.core.event.UserCreatedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.reader.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;
import com.sismics.util.EnvironmentUtil;
import com.sismics.util.LocaleUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User REST resources.
 *
 * @author jtremeaux
 */
@Path("/user")
public class UserResource extends BaseResource {

    /**
     * Creates a new user.
     *
     * @param json Request JSON object
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(JSONObject json) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the input data
        String username = ValidationUtil.validateUsername(json.has("username") ? json.getString("username") : null, 3, 50);
        String password = ValidationUtil.validatePassword(json.has("password") ? json.getString("password") : null, 8, 50);
        String email = ValidationUtil.validateEmail(json.has("email") ? json.getString("email") : null, 3, 50);
        String localeId = ValidationUtil.validateLocale(json.has("locale") ? json.getString("locale") : null, true);

        // Create the user
        User user = new User();
        user.setRoleId(SecurityConfig.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDisplayTitleWeb(false);
        user.setDisplayTitleMobile(true);
        user.setDisplayUnreadWeb(true);
        user.setDisplayUnreadMobile(true);
        user.setCreateDate(new Date());
        user.setLocaleId(localeId);

        // Create the user
        UserDao userDao = new UserDao();
        String userId;
        try {
            userId = userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }

        // Create the root category for this user
        Category category = new Category();
        category.setUserId(userId);
        category.setOrder(0);

        CategoryDao categoryDao = new CategoryDao();
        categoryDao.create(category);

        // Raise a user creation event
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUser(user);
        AppContext.getInstance().getMailEventBus().post(userCreatedEvent);

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates user informations.
     *
     * @param json Request JSON object
     * @return Response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(JSONObject json) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate the input data
        String password = ValidationUtil.validatePassword(json.has("password") ? json.getString("password") : null, 8, 50, true);
        String email = ValidationUtil.validateEmail(json.has("email") ? json.getString("email") : null, 3, 50, true);
        String themeId = ValidationUtil.validateTheme(EnvironmentUtil.isUnitTest() ? null : request.getServletContext(), json.has("theme") ? json.getString("theme") : null, "theme", true);
        String localeId = ValidationUtil.validateLocale(json.has("locale") ? json.getString("locale") : null, true);
        Boolean displayTitleWeb = json.has("display_title_web") ? json.getBoolean("display_title_web") : null;
        Boolean displayTitleMobile = json.has("display_title_mobile") ? json.getBoolean("display_title_mobile") : null;
        Boolean displayUnreadWeb = json.has("display_unread_web") ? json.getBoolean("display_unread_web") : null;
        Boolean displayUnreadMobile = json.has("display_unread_mobile") ? json.getBoolean("display_unread_mobile") : null;
        Boolean narrowArticle = json.has("narrow_article") ? json.getBoolean("narrow_article") : null;
        Boolean firstConnection = json.has("first_connection") && hasBaseFunction(BaseFunction.ADMIN) ? json.getBoolean("first_connection") : null;

        // Update the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        if (displayTitleWeb != null) {
            user.setDisplayTitleWeb(displayTitleWeb);
        }
        if (displayTitleMobile != null) {
            user.setDisplayTitleMobile(displayTitleMobile);
        }
        if (displayUnreadWeb != null) {
            user.setDisplayUnreadWeb(displayUnreadWeb);
        }
        if (displayUnreadMobile != null) {
            user.setDisplayUnreadMobile(displayUnreadMobile);
        }
        if (narrowArticle != null) {
            user.setNarrowArticle(narrowArticle);
        }
        if (firstConnection != null) {
            user.setFirstConnection(firstConnection);
        }
        user.setModifiedDate(new Date());
        userDao.update(user);

        if (password != null) {
            // Update the password
            PasswordChangedEvent passwordChangedEvent = new PasswordChangedEvent();
            passwordChangedEvent.setUserId(user.getUserId());
            AppContext.getInstance().getMailEventBus().post(passwordChangedEvent);
        }

        // Raise a user update event
        UserUpdatedEvent userUpdatedEvent = new UserUpdatedEvent();
        userUpdatedEvent.setUser(user);
        AppContext.getInstance().getMailEventBus().post(userUpdatedEvent);

        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
```