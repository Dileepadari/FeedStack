package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

public class PasswordValidator extends BaseValidator {
    private static final int LENGTH_MIN = 8;

    private static final int LENGTH_MAX = 50;

    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        // Strip first, so " 12345678 " is the 8 character password it looks like.
        String password = StringUtils.strip(request.getPassword());
        request.setPassword(password);

        if (StringUtils.isEmpty(password)) {
            throw new ValidationException("password must be set");
        }
        if (password.length() < LENGTH_MIN) {
            throw new ValidationException(
                    MessageFormat.format("password must be more than {0} characters", LENGTH_MIN));
        }
        if (password.length() > LENGTH_MAX) {
            throw new ValidationException(
                    MessageFormat.format("password must be less than {0} characters", LENGTH_MAX));
        }

        validateNext(request);
    }
}
