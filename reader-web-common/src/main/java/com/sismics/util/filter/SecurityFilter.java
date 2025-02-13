```java
package com.sismics.util.filter;

import com.sismics.reader.core.constant.DefaultConfig;
import com.sismics.reader.core.dao.jpa.*;
import com.sismics.reader.core.model.jpa.*;
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

public class SecurityFilter implements Filter {


    public static final String PRINCIPAL_ATTRIBUTE = "principal";

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


    protected static boolean hasIdentifiedUser(HttpServletRequest request) {
        return request.getAttribute(PRINCIPAL_ATTRIBUTE) instanceof UserPrincipal;
    }


    protected static void injectUser(HttpServletRequest request, User user) {
        if (user != null && user.getDeleteDate() == null) {
            injectAuthenticatedUser(request, user);
        } else {
            injectAnonymousUser(request);
        }
    }


    protected static void injectAuthenticatedUser(HttpServletRequest request, User authenticatedUser) {
        UserPrincipal userPrincipal = new UserPrincipal(authenticatedUser.getId(), authenticatedUser.getUsername());
        Locale locale = LocaleUtil.getLocale(authenticatedUser.getLocaleId());
        userPrincipal.setLocale(locale);
        RoleBaseFunctionDao userBaseFunction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFunction.findByRoleId(authenticatedUser.getRoleId());
        userPrincipal.setBaseFunctionSet(baseFunctionSet);
        request.setAttribute(PRINCIPAL_ATTRIBUTE, userPrincipal);
    }


    protected static void injectAnonymousUser(HttpServletRequest request) {
        AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();
        anonymousPrincipal.setLocale(request.getLocale());
        anonymousPrincipal.setDateTimeZone(DateTimeZone.forID(DefaultConfig.DEFAULT_TIMEZONE_ID));
        request.setAttribute(PRINCIPAL_ATTRIBUTE, anonymousPrincipal);
    }


    protected abstract User authenticate(HttpServletRequest request);

}
```