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
     * Display title for the web version.
     */
    @Column(name = "display_title_web", nullable = false)
    private boolean displayTitleWeb;

    /**
     * Display title for the mobile version.
     */
    @Column(name = "display_title_mobile", nullable = false)
    private boolean displayTitleMobile;

    /**
     * Display number of unread feeds for the web version.
     */
    @Column(name = "display_unread_web", nullable = false)
    private boolean displayUnreadWeb;

    /**
     * Display number of unread feeds for the mobile version.
     */
    @Column(name = "display_unread_mobile", nullable = false)
    private boolean displayUnreadMobile;

    /**
     * Narrow article.
     */
    @Column(name = "narrow_article", nullable = false)
    private boolean narrowArticle;

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
     * Is display title for the web version enabled?
     *
     * @return True if display title for the web version is enabled.
     */
    public boolean isDisplayTitleWeb() {
        return displayTitleWeb;
    }

    /**
     * Enable/disable display title for the web version.
     *
     * @param displayTitleWeb True to enable display title for the web version.
     */
    public void setDisplayTitleWeb(boolean displayTitleWeb) {
        this.displayTitleWeb = displayTitleWeb;
    }

    /**
     * Is display title for the mobile version enabled?
     *
     * @return True if display title for the mobile version is enabled.
     */
    public boolean isDisplayTitleMobile() {
        return displayTitleMobile;
    }

    /**
     * Enable/disable display title for the mobile version.
     *
     * @param displayTitleMobile True to enable display title for the mobile version.
     */
    public void setDisplayTitleMobile(boolean displayTitleMobile) {
        this.displayTitleMobile = displayTitleMobile;
    }

    /**
     * Is display number of unread feeds for the web version enabled?
     *
     * @return True if display number of unread feeds for the web version is enabled.
     */
    public boolean isDisplayUnreadWeb() {
        return displayUnreadWeb;
    }

    /**
     * Enable/disable display number of unread feeds for the web version.
     *
     * @param displayUnreadWeb True to enable display number of unread feeds for the web version.
     */
    public void setDisplayUnreadWeb(boolean displayUnreadWeb) {
        this.displayUnreadWeb = displayUnreadWeb;
    }

    /**
     * Is display number of unread feeds for the mobile version enabled?
     *
     * @return True if display number of unread feeds for the mobile version is enabled.
     */
    public boolean isDisplayUnreadMobile() {
        return displayUnreadMobile;
    }

    /**
     * Enable/disable display number of unread feeds for the mobile version.
     *
     * @param displayUnreadMobile True to enable display number of unread feeds for the mobile version.
     */
    public void setDisplayUnreadMobile(boolean displayUnreadMobile) {
        this.displayUnreadMobile = displayUnreadMobile;
    }

    /**
     * Is narrow article enabled?
     *
     * @return True if narrow article is enabled.
     */
    public boolean isNarrowArticle() {
        return narrowArticle;
    }

    /**
     * Enable/disable narrow article.
     *
     * @param narrowArticle True to enable narrow article.
     */
    public void setNarrowArticle(boolean narrowArticle) {
        this.narrowArticle = narrowArticle;
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