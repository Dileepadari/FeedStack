package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;

public class PasswordValidator extends BaseValidator {
    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        // Validate password
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (request.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
        // Add more password validation rules as needed

        validateNext(request);
    }
}