```java
package com.sismics.reader.core.model.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 256;
    private static final int MAX_EMAIL_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id", nullable = false)
    private String id;

    @Column(name = "username", nullable = false, length = MAX_USERNAME_LENGTH, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    @Column(name = "email", nullable = false, length = MAX_EMAIL_LENGTH, unique = true)
    private String email;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "first_connection", nullable = false)
    private boolean firstConnection;

    public static Builder builder() {
        return new Builder();
    }

    @OneToMany(mappedBy = "user")
    private Set<AuthenticationToken> authenticationTokens = new HashSet<>();

    public User() {
    }

    public User(String id, String username, String password, String email, Date createDate, Date modifiedDate, boolean firstConnection, Set<AuthenticationToken> authenticationTokens) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
        this.firstConnection = firstConnection;
        this.authenticationTokens = authenticationTokens;
    }

    public String getId() {
        return id;
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
        if (createDate != null ? !createDate.equals(user.createDate) : user.createDate != null) return false;
        if (modifiedDate != null ? !modifiedDate.equals(user.modifiedDate) : user.modifiedDate != null) return false;
        return authenticationTokens != null ? authenticationTokens.equals(user.authenticationTokens) : user.authenticationTokens == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (firstConnection ? 1 : 0);
        result = 31 * result + (authenticationTokens != null ? authenticationTokens.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createDate=" + createDate +
                ", modifiedDate=" + modifiedDate +
                ", firstConnection=" + firstConnection +
                ", authenticationTokens=" + authenticationTokens +
                '}';
    }

    public static class Builder {
        private String id;
        private String username;
        private String password;
        private String email;
        private Date createDate;
        private Date modifiedDate;
        private boolean firstConnection;
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
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder createDate(Date createDate) {
            this.createDate = createDate;
            return this;
        }

        public Builder modifiedDate(Date modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder firstConnection(boolean firstConnection) {
            this.firstConnection = firstConnection;
            return this;
        }

        public Builder authenticationTokens(Set<AuthenticationToken> authenticationTokens) {
            this.authenticationTokens = authenticationTokens;
            return this;
        }

        public User build() {
            return new User(id, username, password, email, createDate, modifiedDate, firstConnection, authenticationTokens);
        }
    }
}
```
====FILE_DELIMITER====
```java
package com.sismics.reader.core.model.jpa;

import com.sismics.reader.core.model.UserAuthenticationToken;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken implements Serializable, UserAuthenticationToken {

    private static final int MAX_TOKEN_LENGTH = 256;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "token_id", nullable = false)
    private String id;

    @Column(name = "token", nullable = false, length = MAX_TOKEN_LENGTH)
    private String token;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public AuthenticationToken() {
    }

    public AuthenticationToken(String id, String token, Date createDate, User user) {
        this.id = id;
        this.token = token;
        this.createDate = createDate;
        this