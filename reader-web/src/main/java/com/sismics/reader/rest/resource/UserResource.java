```java
package com.sismics.reader.core.model.jpa;

import com.sismics.reader.core.model.UserAuthenticationToken;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken implements Serializable, UserAuthenticationToken {

    private static final int MAX_TOKEN_LENGTH = 256;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authentication_tokens_token_id_seq")
    @SequenceGenerator(name = "authentication_tokens_token_id_seq", sequenceName = "authentication_tokens_token_id_seq", allocationSize = 1)
    @Column(name = "token_id", nullable = false)
    private String id;

    @Column(name = "token", nullable = false, length = MAX_TOKEN_LENGTH)
    private String token;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public AuthenticationToken() {
    }

    public AuthenticationToken(String id, String token, Date createDate, User user) {
        this.id = id;
        this.token = token;
        this.createDate = createDate;
        this.user = user;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Optional<String> getUserId() {
        return Optional.ofNullable(user).map(User::getId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthenticationToken that = (AuthenticationToken) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (token != null ? !token.equals(that.token) : that.token != null) {
            return false;
        }
        if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null) {
            return false;
        }
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthenticationToken{" +
                "id='" + id + '\'' +
                ", token='" + token + '\'' +
                ", createDate=" + createDate +
                ", user=" + user +
                '}';
    }
}
```====FILE_DELIMITER====
```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 256;
    private static final int MAX_EMAIL_LENGTH = 50;

    @Id
    @Column(name = "user_id", nullable = false)
    private String id;

    @Column(name = "username", nullable = false, length = MAX_USERNAME_LENGTH, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    @Column(name = "email", nullable = false, length = MAX_EMAIL_LENGTH, unique = true)
    private String email;

    @Column(name = "create_date", nullable = false)
    private java.sql.Date createDate;

    @Column(name = "modified_date")
    private java.sql.Date modifiedDate;

    @Column(name = "first_connection", nullable = false)
    private boolean firstConnection;

    public User() {
    }

    public User(String id, String username, String password, String email, java.sql.Date createDate, java.sql.Date modifiedDate, boolean firstConnection) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createDate = createDate;
        this.modifiedDate = modifiedDate;
        this.firstConnection = firstConnection;
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

    public java.sql.Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(java.sql.Date createDate) {
        this.createDate = createDate;
    }

    public java.sql.Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(java.sql.Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isFirstConnection() {
        return firstConnection;
    }

    public void setFirstConnection(boolean firstConnection) {
        this.firstConnection = firstConnection;
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
        return modifiedDate != null ? modifiedDate.equals(user.modifiedDate) : user.modifiedDate == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode