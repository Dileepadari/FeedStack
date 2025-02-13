```java
package com.sismics.util.filter;

import com.sismics.reader.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.model.jpa.AuthenticationToken;
import com.sismics.reader.core.model.jpa.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;

/**
 * This filter is used to authenticate the user having an active session via an authentication token stored in database.
 * The filter extracts the authentication token stored in a cookie.
 * If the cookie exists and the token is valid, the filter injects a UserPrincipal into a request attribute.
 * If not, the user is anonymous, and the filter injects a AnonymousPrincipal into the request attribute.
 *
 * @author jtremeaux
 * @author pacien
 */
public class TokenBasedSecurityFilter extends SecurityFilter {

    /**
     * Name of the cookie used to store the authentication token.
     */
    public static final String COOKIE_NAME = "auth_token";

    /**
     * Lifetime of the authentication token in seconds, since login.
     */
    public static final int TOKEN_LONG_LIFETIME = 3600 * 24 * 365 * 20;

    /**
     * Lifetime of the authentication token in seconds, since last connection.
     */
    public static final int TOKEN_SESSION_LIFETIME = 3600 * 24;


    @Override
    public User authenticate(HttpServletRequest request) {

        // Extract the authentication token from the request
        String authTokenId = extractAuthToken(request.getCookies());
        if (authTokenId == null) {
            return null;
        }

        // Retrieve the authentication token from the database
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = authenticationTokenDao.get(authTokenId);
        if (authenticationToken == null) {
            return null;
        }

        // Handle expired token
        if (isTokenExpired(authenticationToken)) {
            handleExpiredToken(authenticationTokenDao, authTokenId);
            return null;
        }

        // Update the last connection date of the token
        authenticationTokenDao.updateLastConnectionDate(authenticationToken.getId());

        // Retrieve the user from the database using the user id from the token
        String userId = authenticationToken.getUserId();
        UserDao userDao = new UserDao();
        User user = userDao.getById(userId);

        // Return the authenticated user
        return user;
    }


    private static String extractAuthToken(Cookie[] cookies) {

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    private static boolean isTokenExpired(AuthenticationToken authenticationToken) {

        long now = new Date().getTime();
        long creationDate = authenticationToken.getCreationDate().getTime();
        if (authenticationToken.isLongLasted()) {
            return now >= creationDate + ((long) TOKEN_LONG_LIFETIME) * 1000L;
        } else {
            long lastConnectionDate = authenticationToken.getLastConnectionDate() != null ?
                    authenticationToken.getLastConnectionDate().getTime() : creationDate;
            return now >= lastConnectionDate + ((long) TOKEN_SESSION_LIFETIME) * 1000L;
        }
    }


    private static void handleExpiredToken(AuthenticationTokenDao authenticationTokenDao, String authTokenId) {

        try {
            authenticationTokenDao.delete(authTokenId);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(MessageFormat.format("Error deleting authentication token {0} ", authTokenId), e);
            }
        }
    }
}
```