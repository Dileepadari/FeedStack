```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.constant.DefaultConfig;
import com.sismics.reader.core.dao.jpa.criteria.UserCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.dao.jpa.mapper.UserMapper;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.core.model.jpa.User_;
import com.sismics.util.context.ThreadLocalContext;
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
                .filter(user -> BCrypt.checkpw(password, user.getPassword()))
                .map(User::getId)
                .orElse(null);
    }

    public String create(User user) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Create the user UUID
        user.setId(UUID.randomUUID().toString());

        // Checks for user unicity
        if (userExists(user.getUsername())) {
            throw new Exception("AlreadyExistingUsername");
        }

        prepareUser(user);
        em.persist(user);

        return user.getId();
    }

    public User update(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        User userFromDb = em.find(User.class, user.getId());

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
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        User userFromDb = em.find(User.class, user.getId());

        // Update the user
        userFromDb.setPassword(hashPassword(user.getPassword()));

        return userFromDb;
    }

    public User getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(User.class, id);
    }

    public User getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.createQuery("select u from User u where u.username = :username and u.delete_date is null", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User getActiveByPasswordResetKey(String passwordResetKey) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.createQuery("select u from User u where u.password_reset_key = :passwordResetKey and u.delete_date is null", User.class)
                    .setParameter("passwordResetKey", passwordResetKey)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        User userFromDb = em.createQuery("select u from User u where u.username = :username and u.delete_date is null", User.class)
                .setParameter("username", username)
                .getSingleResult();

        // Delete the user
        Date dateNow = new Date();
        userFromDb.setDelete_date(dateNow);

        // Delete linked data
        em.createQuery("delete from AuthenticationToken at where at.user_id = :userId")
                .setParameter("userId", userFromDb.getId())
                .executeUpdate();

        em.createQuery("update UserArticle ua set ua.delete_date = :dateNow where ua.user_id = :userId and ua.delete_date is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();

        em.createQuery("update FeedSubscription fs set fs.delete_date = :dateNow where fs.user_id = :userId and fs.delete_date is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();

        em.createQuery("update Category c set c.delete_date = :dateNow where c.user_id = :userId and c.delete_date is null")
                .setParameter("userId", userFromDb.getId())
                .setParameter("dateNow", dateNow)
                .executeUpdate();
    }

    protected Optional<User> findUser(String email) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return Optional.of(em.createQuery("select u from User u where u.email = :email and u.delete_date is null", User.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    protected boolean userExists(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return !em.createQuery("select u from User u where u.username = :username and u.delete_date is null", User.class)
                .setParameter("username", username)
                .getResultList()
                .isEmpty();
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