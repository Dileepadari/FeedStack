```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id", nullable = false)
    private String id;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 256)
    private String password;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "locale_id", length = 5)
    private String localeId;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "first_connection", nullable = false)
    private boolean firstConnection;

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "last_login_date")
    private Date lastLoginDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<AuthenticationToken> authenticationTokens = new HashSet<>();

    public User() {
    }

    private User(String id, String username, String password, String email, String roleId, String localeId, Date createDate, Date modifiedDate, boolean firstConnection, String theme, Date lastLoginDate, Set<AuthenticationToken> authenticationTokens) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.roleId = roleId;
        this.localeId = localeId;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
        this.firstConnection = firstConnection;
        this.theme = theme;
        this.lastLoginDate = lastLoginDate;
        this.authenticationTokens = authenticationTokens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getLocaleId() {
        return localeId;
    }

    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isFirstConnection() {
        return firstConnection;
    }

    public void setFirstConnection(boolean firstConnection) {
        this.firstConnection = firstConnection;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Set<AuthenticationToken> getAuthenticationTokens() {
        return authenticationTokens;
    }

    public void setAuthenticationTokens(Set<AuthenticationToken> authenticationTokens) {
        this.authenticationTokens = authenticationTokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (firstConnection != user.firstConnection) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (roleId != null ? !roleId.equals(user.roleId) : user.roleId != null) return false;
        if (localeId != null ? !localeId.equals(user.localeId) : user.localeId != null) return false;
        if (createDate != null ? !createDate.equals(user.createDate) : user.createDate != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(user.modifiedDate) : user.modifiedDate != null) return false;
        if (theme != null ? !theme.equals(user.theme) : user.theme != null) return false;
        if (lastLoginDate != null ? !lastLoginDate.equals(user.lastLoginDate) : user.lastLoginDate != null) return false;
        return authenticationTokens != null ? authenticationTokens.equals(user.authenticationTokens) : user.authenticationTokens == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        result = 31 * result + (localeId != null ? localeId.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (firstConnection ? 1 : 0);
        result = 31 * result + (theme != null ? theme.hashCode() : 0);
        result = 31 * result + (lastLoginDate != null ? lastLoginDate.hashCode() : 0);
        result = 31 * result + (authenticationTokens != null ? authenticationTokens.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", roleId='" + roleId + '\'' +
                ", localeId='" + localeId + '\'' +
                ", createDate=" + createDate +
                ", modifiedDate=" + modifiedDate +
                ", firstConnection=" + firstConnection +
                ", theme='" + theme + '\'' +
                ", lastLoginDate=" + lastLoginDate +
                ", authenticationTokens=" + authenticationTokens +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String password;
        private String email;
        private String roleId;
        private String localeId;
        private Date createDate;
        private Date modifiedDate;
        private boolean firstConnection;
        private String theme;
        private Date lastLoginDate;
        private Set<AuthenticationToken> authenticationTokens;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;