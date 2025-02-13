```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.constant.DefaultConfig;
import com.sismics.reader.core.dao.jpa.criteria.UserCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.dao.jpa.mapper.UserMapper;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.model.jpa.User_;
import com.sismics.reader.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class UserDao extends BaseDao<UserDto, UserCriteria> {

    @Override
    protected QueryParam getQueryParam(UserCriteria criteria, FilterCriteria filterCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        StringBuilder query = new StringBuilder("select u.id as id, u.username as username, u.email as email, u.created_date as created_date, u.id_locale as id_locale")
                .append(" from User u ");

        // Add search criterias
        criteriaList.add("u.delete_date is null");

        return new QueryParam(query.toString(), criteriaList, parameterMap, null, filterCriteria, new UserMapper());
    }

    public String authenticate(String email, String password) {
        return findUser(email)
                .map(user -> BCrypt.checkpw(password, user.getPassword()) ? user.getId() : null)
                .orElse(null);
    }

    public String create(User user) throws Exception {
        user.setId(UUID.randomUUID().toString());

        // Checks for user unicity
        if (userExists(user.getUsername())) {
            throw new Exception("AlreadyExistingUsername");
        }

        prepareUser(user);
        insert(user);

        return user.getId();
    }

    public User update(User user) {
        User userFromDb = getById(user.getId());

        // Update the user
        userFromDb.setId_locale(user.getId_locale());
        userFromDb.setEmail(user.getEmail());
        userFromDb.setTheme(user.getTheme());
        userFromDb.setDisplay_title_web(user.isDisplay_title_web());
        userFromDb.setDisplay_title_mobile(user.isDisplay_title_mobile());
        userFromDb.setDisplay_unread_web(user.isDisplay_unread_web());
        userFromDb.setDisplay_unread_mobile(user.isDisplay_unread_mobile());
        userFromDb.setFirst_connection(user.isFirst_connection());

        return userFromDb;
    }

    public User updatePassword(User user) {
        User userFromDb = getById(user.getId());

        // Update the user
        userFromDb.setPassword(hashPassword(user.getPassword()));

        return userFromDb;
    }

    public User getById(String id) {
        return find(id);
    }

    public User getActiveByUsername(String username) {
        try {
            return find(User_.username.eq(username));
        } catch (NoResultException e) {
            return null;
        }
    }

    public User getActiveByPasswordResetKey(String passwordResetKey) {
        try {
            return find(User_.password_reset_key.eq(passwordResetKey));
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(String username) {
        User userFromDb = getActiveByUsername(username);

        // Delete the user
        Date dateNow = new Date();
        userFromDb.setDelete_date(dateNow);

        // Delete linked data
        deleteAll(User.AuthenticationToken_.user.eq(userFromDb));
        deleteAll(User.UserArticle_.user.eq(userFromDb));
        deleteAll(User.FeedSubscription_.user.eq(userFromDb));
        deleteAll(User.Category_.user.eq(userFromDb));
    }

    protected Optional<User> findUser(String email) {
        try {
            return Optional.of(find(User_.email.eq(email)));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    protected boolean userExists(String username) {
        return !findAll(User_.username.eq(username)).isEmpty();
    }

    protected void prepareUser(User user) {
        user.setCreated_date(new Date());
        user.setPassword(hashPassword(user.getPassword()));
        user.setTheme(DefaultConfig.DEFAULT_THEME_ID);
    }

    protected String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
====FILE_DELIMITER====
package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.model.jpa.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface UserMapper {

    @Mappings({
            @Mapping(source = User_.ID, target = UserDto.ID),
            @Mapping(source = User_.USERNAME, target = UserDto.USERNAME),
            @Mapping(source = User_.EMAIL, target = UserDto.EMAIL),
            @Mapping(source = User_.CREATED_DATE, target = UserDto.CREATED_DATE),
            @Mapping(source = User_.ID_LOCALE, target = UserDto.ID_LOCALE)
    })
    UserDto toDto(User user);
}
```