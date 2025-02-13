```java
package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "authentication_tokens")
public class AuthenticationToken {

    public static final int MAX_TOKEN_LENGTH = 256;
    
    @Id
    @Column(name = "token_id", nullable = false)
    public String id;

    @Column(name = "token", nullable = false, length = MAX_TOKEN_LENGTH)
    public String token;

    @Column(name = "create_date", nullable = false)
    public Date createDate;

    @Column(name = "user_id")
    public String userId;

    // Getters and setters omitted
}
====FILE_DELIMITER====
package com.sismics.reader.core.model.jpa;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "users")
public class User {

    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_PASSWORD_LENGTH = 256;
    public static final int MAX_EMAIL_LENGTH = 50;
    
    @Id
    @Column(name = "user_id", nullable = false)
    public String id;

    @Column(name = "username", nullable = false, length = MAX_USERNAME_LENGTH, unique = true)
    public String username;

    @Column(name = "password", nullable = false, length = MAX_PASSWORD_LENGTH)
    public String password;

    @Column(name = "email", nullable = false, length = MAX_EMAIL_LENGTH, unique = true)
    public String email;

    @Column(name = "create_date", nullable = false)
    public Date createDate;

    @Column(name = "modified_date")
    public Date modifiedDate;

    @Column(name = "first_connection", nullable = false)
    public boolean firstConnection;

    // Getters and setters omitted
}
```