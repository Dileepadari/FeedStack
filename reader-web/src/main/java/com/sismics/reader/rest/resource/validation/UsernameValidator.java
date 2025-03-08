package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;

public class UsernameValidator extends BaseValidator {
    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        // Validate username
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }
        // Add more username validation rules as needed

        validateNext(request);
    }
}