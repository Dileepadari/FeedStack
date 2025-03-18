package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.core.event.UserCreatedEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.User;
import com.sismics.reader.rest.resource.validation.*;

public class RegistrationFacade {
    private final UserDao userDao;
    private final CategoryDao categoryDao;
    private final Validator validationChain;

    public RegistrationFacade() {
        this.userDao = new UserDao();
        this.categoryDao = new CategoryDao();

        //setting the chain
        UsernameValidator usernameValidator = new UsernameValidator();
        PasswordValidator passwordValidator = new PasswordValidator();
        EmailValidator emailValidator = new EmailValidator();

        usernameValidator.setNext(passwordValidator);
        passwordValidator.setNext(emailValidator);

        this.validationChain = usernameValidator;
    }

    /**
     * Handles the entire user registration process
     *
     * @param username the username for the new user
     * @param password the password for the new user
     * @param email the email for the new user
     * @param localeId the locale ID for the new user
     * @throws ValidationException if input validation fails
     * @throws UserExistsException if username already exists
     * @throws Exception for other errors
     */
    public void registerUser(String username, String password, String email, String localeId)
            throws ValidationException, UserExistsException, Exception {
        // New Request
        RegistrationRequest request = new RegistrationRequest(username, password, email, localeId);

        //validate
        validationChain.validate(request);
        // Create user
        User user = User.createNewUser(username, password, email, localeId, false);

        // Persist user
        String userId = createUserInDatabase(user);

        // Create root category
        createRootCategory(userId);

        // Send notification
        notifyUserCreation(user);
    }


    private String createUserInDatabase(User user) throws UserExistsException, Exception {
        try {
            return userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new UserExistsException("Username already exists", e);
            }
            throw e;
        }
    }

    private void createRootCategory(String userId) {
        Category category = new Category();
        category.setUserId(userId);
        category.setOrder(0);
        categoryDao.create(category);
    }

    private void notifyUserCreation(User user) {
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUser(user);
        try {
            AppContext.getInstance().getEventBusManager().getMailEventBus().post(userCreatedEvent);
        } catch (Exception e) {
            // Log the exception but don't interrupt the registration process
            e.printStackTrace();
        }
    }
}
