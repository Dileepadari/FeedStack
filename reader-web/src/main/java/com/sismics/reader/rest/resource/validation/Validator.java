package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;

public interface Validator {
    void validate(RegistrationRequest request) throws ValidationException;
    void setNext(Validator next);
}
