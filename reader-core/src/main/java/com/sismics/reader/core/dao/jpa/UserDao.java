```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.constant.DefaultConfig;
import com.sismics.reader.core.dao.jpa.criteria.UserCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserDto;
import com.sismics.reader.core.dao.jpa.mapper.UserMapper;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * User DAO.
 *
 * @author jtremeaux
 */
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

    /**
     * Authenticates an user.
     *
     * @param username User login
     * @param password User password
     * @return ID of the authenticated user or null
     */
    public String authenticate(String username, String password) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query query = em.createQuery("select u from User u where u.username = :username and u.delete_date is null")
                    .setParameter("username", username);
            User user = (User) query.getSingleResult();
            if (!BCrypt.checkpw(password, user.getPassword())) {
                return null;
            }
            return user.getId();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Creates a new user.
     *
     * @param user User to create
     * @return User ID
     */
    public String create(User user) throws Exception {
        // Create the user UUID
        user.setId(UUID.randomUUID().toString());

        // Checks for user unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery("select u from User u where u.username = :username and u.delete_date is null")
                .setParameter("username", user.getUsername());
        List<?> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            throw new Exception("AlreadyExistingUsername");
        }

        user.setCreated_date(new Date());
        user.setPassword(hashPassword(user.getPassword()));
        user.setTheme(DefaultConfig.DEFAULT_THEME_ID);
        em.persist(user);

        return user.getId();
    }

    /**
     * Updates a user.
     *
     * @param user User to update
     * @return Updated user
     */
    public User update(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        Query query = em.createQuery("select u from User u where u.id = :id and u.delete_date is null")
                .setParameter("id", user.getId());
        User userFromDb = (User) query.getSingleResult();

        // Update the user
        userFromDb.setId_locale(user.getId_locale());
        userFromDb.setEmail(user.getEmail());
        userFromDb.setTheme(user.getTheme());
        userFromDb.setDisplay_title_web(user.isDisplay_title_web());
        userFromDb.setDisplay_title_mobile(user.isDisplay_title_mobile());
        userFromDb.setDisplay_unread_web(user.isDisplay_unread_web());
        userFromDb.setDisplay_unread_mobile(user.isDisplay_unread_mobile());
        userFromDb.setFirst_connection(user.isFirst_connection());

        return user;
    }

    /**
     * Update the user password.
     *
     * @param user User to update
     * @return Updated user
     */
    public User updatePassword(User user) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        Query query = em.createQuery("select u from User u where u.id = :id and u.delete_date is null")
                .setParameter("id", user.getId());
        User userFromDb = (User) query.getSingleResult();

        // Update the user
        userFromDb.setPassword(hashPassword(user.getPassword()));

        return user;
    }

    /**
     * Gets a user by its ID.
     *
     * @param id User ID
     * @return User
     */
    public User getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(User.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets an active user by its username.
     *
     * @param username User's username
     * @return User
     */
    public User getActiveByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query query = em.createQuery("select u from User u where u.username = :username and u.delete_date is null")
                    .setParameter("username", username);
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Gets an active user by its password recovery token.
     *
     * @param passwordResetKey Password recovery token
     * @return User
     */
    public User getActiveByPasswordResetKey(String passwordResetKey) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query query = em.createQuery("select u from User u where u.password_reset_key = :passwordResetKey and u.delete_date is null")
                    .setParameter("passwordResetKey", passwordResetKey);
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Deletes a user.
     *
     * @param username User's username
     */
    public void delete(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the user
        User userFromDb = (User) em.createQuery("select u from User u where u.username = :username and u.delete_date is null")
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

    /**
     * Hash the user's password.
     *
     * @param password Clear password
     * @return Hashed password
     */
    protected String hashPassword(