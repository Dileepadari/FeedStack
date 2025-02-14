package com.sismics.reader.rest.resource;

import com.sismics.security.IPrincipal;
import com.sismics.util.filter.SecurityFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.security.Principal;

public abstract class AuthenticatedResource {
    @Context
    protected HttpServletRequest request;

    protected IPrincipal principal;

    /**
     * Check if user is authenticated.
     */
    protected boolean authenticate() {
        Principal principal = (Principal) request.getAttribute(SecurityFilter.PRINCIPAL_ATTRIBUTE);
        if (principal instanceof IPrincipal) {
            this.principal = (IPrincipal) principal;
            return !this.principal.isAnonymous();
        }
        return false;
    }
}