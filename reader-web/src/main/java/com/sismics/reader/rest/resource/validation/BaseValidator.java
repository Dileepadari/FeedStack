package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;

public abstract class BaseValidator implements Validator {
    protected Validator nextValidator;

    @Override
    public void setNext(Validator next) {
        this.nextValidator = next;
    }

    protected void validateNext(RegistrationRequest request) throws ValidationException {
        if (nextValidator != null) {
            nextValidator.validate(request);
        }
    }
}