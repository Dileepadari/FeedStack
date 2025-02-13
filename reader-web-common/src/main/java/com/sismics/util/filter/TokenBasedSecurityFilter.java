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

public class TokenBasedSecurityFilter extends SecurityFilter {


    public static final String COOKIE_NAME = "auth_token";


    public static final int TOKEN_LONG_LIFETIME = 3600 * 24 * 365 * 20;


    public static final int TOKEN_SESSION_LIFETIME = 3600 * 24;


    @Override
    public User authenticate(HttpServletRequest request) {
        String authTokenId = extractAuthToken(request.getCookies());
        if (authTokenId == null) {
            return null;
        }
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = authenticationTokenDao.get(authTokenId);
        if (authenticationToken == null) {
            return null;
        }
        if (isTokenExpired(authenticationToken)) {
            handleExpiredToken(authenticationTokenDao, authTokenId);
            return null;
        }
        authenticationTokenDao.updateLastConnectionDate(authenticationToken.getId());
        String userId = authenticationToken.getUserId();
        UserDao userDao = new UserDao();
        User user = userDao.getById(userId);
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
                LOG.error(MessageFormat.format("Error deleting authentication token with id: {0}", authTokenId), e);
            }
        }
    }

}

```