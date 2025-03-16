package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.core.dao.jpa.UserDao;
import com.sismics.reader.rest.resource.ValidationException;

public class EmailValidator extends BaseValidator {
    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }

        // Check if email is already in use
        UserDao userDao = new UserDao();
        try {
            if (userDao.getByEmail(request.getEmail().trim()) != null) {
                throw new ValidationException("Email address already in use");
            }
        } catch (Exception e) {
            throw new ValidationException("Error checking email uniqueness: " + e.getMessage());
        }

        validateNext(request);
    }
}