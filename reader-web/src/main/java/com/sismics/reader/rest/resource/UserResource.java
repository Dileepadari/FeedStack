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

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "first_connection", nullable = false)
    private boolean firstConnection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<AuthenticationToken> authenticationTokens = new HashSet<>();

    // Getters and Setters (omitted)


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
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", createDate=" + createDate +
                ", modifiedDate=" + modifiedDate +
                ", firstConnection=" + firstConnection +
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

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "token_id", nullable = false)
    private String id;

    @Column(name = "token", nullable = false, length = 256)
    private String token;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and Setters (omitted)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticationToken that = (AuthenticationToken) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null) return false;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String token;
        private Date createDate;
        private User user;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder createDate(Date createDate) {
            this.createDate = createDate;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public AuthenticationToken build() {
            return new AuthenticationToken(id, token, createDate, user);
        }
    }
}
```