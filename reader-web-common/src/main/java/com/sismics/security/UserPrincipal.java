====FILE_DELIMITER====
```java
package com.sismics.security;

import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTimeZone;

/**
 * Authenticated users principal.
 * 
 * @author jtremeaux
 */
public class UserPrincipal implements IPrincipal {
    private String id;
    private String name;
    private Locale locale;
    private DateTimeZone dateTimeZone;
    private String email;
    private Set<String> baseFunctionSet;

    public UserPrincipal(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(DateTimeZone dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    @Override
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getBaseFunctionSet() {
        return baseFunctionSet;
    }

    public void setBaseFunctionSet(Set<String> baseFunctionSet) {
        this.baseFunctionSet = baseFunctionSet;
    }

}
```