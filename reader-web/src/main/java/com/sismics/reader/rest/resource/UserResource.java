```java
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
     * Role ID.
     */
    @Column(name = "role_id", nullable = false)
    private String roleId;

    /**
     * Username.
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * Password.
     */
    @Column(name = "password", nullable = false, length = 256)
    private String password;

    /**
     * Email.
     */
    @Column(name = "email", nullable = false, length = 50)
    private String email;

    /**
     * Locale ID.
     */
    @Column(name = "locale_id", length = 5)
    private String localeId;
    
    
    /**
     * Creation date.
     */
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    /**
     * Modified date.
     */
    @Column(name = "modified_date")
    private Date modifiedDate;

    /**
     * First connection.
     */
    @Column(name = "first_connection", nullable = false)
    private boolean firstConnection;

    /**
     * Theme.
     */
    @Column(name = "theme", length = 50)
    private String theme;

    /**
     * Last login date.
     */
    @Column(name = "last_login_date")
    private Date lastLoginDate;

    /**
     * Authentication tokens.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<AuthenticationToken> authenticationTokens = new HashSet<>();

    /**
     * Get the user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user ID.
     *
     * @param userId The user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the role ID.
     *
     * @return The role ID.
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Set the role ID.
     *
     * @param roleId The role ID.
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Get the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the email.
     *
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email.
     *
     * @param email The email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the locale ID.
     *
     * @return The locale ID.
     */
    public String getLocaleId() {
        return localeId;
    }

    /**
     * Set the locale ID.
     *
     * @param localeId The locale ID.
     */
    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    /**
     * Get the creation date.
     *
     * @return The creation date.
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Set the creation date.
     *
     * @param createDate The creation date.
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Get the modified date.
     *
     * @return The modified date.
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Set the modified date.
     *
     * @param modifiedDate The modified date.
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Is first connection.
     *
     * @return True if first connection.
     */
    public boolean isFirstConnection() {
        return firstConnection;
    }

    /**
     * Set first connection.
     *
     * @param firstConnection True if first connection.
     */
    public void setFirstConnection(boolean firstConnection) {
        this.firstConnection = firstConnection;
    }

    /**
     * Get the theme.
     *
     * @return The theme.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Set the theme.
     *
     * @param theme The theme.
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Get the last login date.
     *
     * @return The last login date.
     */
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Set the last login date.
     *
     * @param lastLoginDate The last login date.
     */
    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * Get the authentication tokens.
     *
     * @return The authentication tokens.
     */
    public Set<AuthenticationToken> getAuthenticationTokens() {
        return authenticationTokens;
    }

    /**
     * Set the authentication tokens.
     *
     * @param authenticationTokens The authentication tokens.
     */
    public void setAuthenticationTokens(Set<AuthenticationToken> authenticationTokens) {
        this.authenticationTokens = authenticationTokens;
    }


    public static class Builder {
        private String userId;
        private String roleId;
        private String username;
        private String password;
        private String email;
        private String localeId;
        private Date createDate;
        private Date modifiedDate;
        private boolean firstConnection;
        private String theme;
        private Date lastLoginDate;

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setRoleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setLocaleId(String localeId) {
            this.localeId = localeId;
            return this;
        }

        public Builder setCreateDate(Date createDate) {
            this.createDate = createDate;
            return this;
        }

        public Builder setModifiedDate(Date modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder setFirstConnection(boolean firstConnection) {
            this.firstConnection = firstConnection;
            return this;
        }

        public Builder setTheme(String theme) {
            this.theme = theme;
            return this;
        }

        public Builder setLastLoginDate(Date lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
            return this;