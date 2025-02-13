```java
package com.sismics.util.filter;

import com.sismics.reader.core.constant.DefaultConfig;
import com.sismics.reader.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.reader.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.security.AnonymousPrincipal;
import com.sismics.security.UserPrincipal;
import com.sismics.util.LocaleUtil;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * An abstract security filter for user authentication, that injects corresponding users into the request.
 * Successfully authenticated users are injected as UserPrincipal, or as AnonymousPrincipal otherwise.
 * If an user has already been authenticated for the request, no further authentication attempt is made.
 *
 * @author pacien
 * @author jtremeaux
 */
public abstract class SecurityFilter implements Filter {

    /**
     * Name of the attribute containing the principal.
     */
    public static final String PRINCIPAL_ATTRIBUTE = "principal";

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }


    @Override
    public void destroy() {
        // Do nothing
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if (!hasIdentifiedUser(request)) {
            User user = this.authenticate(request);
            injectUser(request, user);
        }

        filterChain.doFilter(request, response);
    }


    /**
     * Determines if the request has an identified user.
     *
     * @param request The HTTP request.
     * @return true if the request has an identified user.
     */
    protected static boolean hasIdentifiedUser(HttpServletRequest request) {
        return request.getAttribute(PRINCIPAL_ATTRIBUTE) instanceof UserPrincipal;
    }


    /**
     * Injects the given user into the request, with the appropriate authentication state.
     *
     * @param request The HTTP request.
     * @param user     The user to inject.
     */
    protected static void injectUser(HttpServletRequest request, User user) {
        // Check if the user is still valid
        if (user != null && user.getDeleteDate() == null) {
            injectAuthenticatedUser(request, user);
        } else {
            injectAnonymousUser(request);
        }
    }


    /**
     * Injects an authenticated user into the request attributes.
     *
     * @param request     The HTTP request.
     * @param authenticatedUser     The authenticated user to inject.
     */
    protected static void injectAuthenticatedUser(HttpServletRequest request, User authenticatedUser) {

        // Create a new user principal
        UserPrincipal userPrincipal = new UserPrincipal(authenticatedUser.getId(), authenticatedUser.getUsername());

        // Add locale
        Locale locale = LocaleUtil.getLocale(authenticatedUser.getLocaleId());
        userPrincipal.setLocale(locale);

        // Add base functions
        RoleBaseFunctionDao userBaseFunction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFunction.findByRoleId(authenticatedUser.getRoleId());
        userPrincipal.setBaseFunctionSet(baseFunctionSet);

        request.setAttribute(PRINCIPAL_ATTRIBUTE, userPrincipal);
    }


    /**
     * Injects an anonymous user into the request attributes.
     *
     * @param request The HTTP request.
     */
    protected static void injectAnonymousUser(HttpServletRequest request) {

        // Create a new anonymous principal
        AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();

        // Set the locale and time zone
        anonymousPrincipal.setLocale(request.getLocale());
        anonymousPrincipal.setDateTimeZone(DateTimeZone.forID(DefaultConfig.DEFAULT_TIMEZONE_ID));

        request.setAttribute(PRINCIPAL_ATTRIBUTE, anonymousPrincipal);
    }


    /**
     * Authenticates an user from the given request parameters.
     *
     * @param request The HTTP request.
     * @return The authenticated user, or null if the authentication failed.
     */
    protected abstract User authenticate(HttpServletRequest request);

}

```