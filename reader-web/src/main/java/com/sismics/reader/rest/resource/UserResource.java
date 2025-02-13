====FILE_DELIMITER====

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
import com.sismics.reader.core.event.UserUpdatedEvent;
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(JSONObject json) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        String username = ValidationUtil.validateUsername(json.has("username") ? json.getString("username") : null, 3, 50);
        String password = ValidationUtil.validatePassword(json.has("password") ? json.getString("password") : null, 8, 50);
        String email = ValidationUtil.validateEmail(json.has("email") ? json.getString("email") : null, 3, 50);
        String localeId = ValidationUtil.validateLocale(json.has("locale") ? json.getString("locale") : null, true);

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

        UserDao userDao = new UserDao();
        String userId = null;
        try {
            userId = userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setOrder(0);

        CategoryDao categoryDao = new CategoryDao();
        categoryDao.create(category);

        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUser(user);
        AppContext.getInstance().getMailEventBus().post(userCreatedEvent);

        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(JSONObject json) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

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
            PasswordChangedEvent passwordChangedEvent = new PasswordChangedEvent();
            passwordChangedEvent.setUserId(user.getUserId());
            AppContext.getInstance().getMailEventBus().post(passwordChangedEvent);
        }

        UserUpdatedEvent userUpdatedEvent = new UserUpdatedEvent();
        userUpdatedEvent.setUser(user);
        AppContext.getInstance().getMailEventBus().post(userUpdatedEvent);

        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
====FILE_DELIMITER====
package com.sismics.reader.core.model.jpa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 *
 * @author jtremeaux
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    /**
     * User ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**